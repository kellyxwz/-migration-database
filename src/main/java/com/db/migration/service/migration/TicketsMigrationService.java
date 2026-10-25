
package com.db.migration.service.migration;

import com.db.migration.model.legacy.ChamadoLegacy;
import com.db.migration.model.legacy.InteracaoLegacy;
import com.db.migration.model.target.MigrationIdMap;
import com.db.migration.model.target.MigrationIssues;
import com.db.migration.model.target.Ticket;
import com.db.migration.repository.legacy.ChamadoLegacyRepository;
import com.db.migration.repository.legacy.InteracaoLegacyRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.MigrationIssuesRepository;
import com.db.migration.repository.target.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketsMigrationService {

    private static final ZoneId ZONE =
            ZoneId.of("America/Sao_Paulo");

    private static final Instant BACKUP_AT =
            LocalDateTime.of(2024, 11, 29, 23, 0)
                    .atZone(ZONE).toInstant();

    private final ChamadoLegacyRepository legacyRepo;
    private final TicketRepository ticketsRepo;
    private final MigrationIdMapRepository mapRepo;
    private final MigrationIssuesRepository issuesRepo;
    private final InteracaoLegacyRepository interacaoRepo;

    public TicketsMigrationService(
            ChamadoLegacyRepository legacyRepo,
            TicketRepository ticketsRepo,
            MigrationIdMapRepository mapRepo,
            MigrationIssuesRepository issuesRepo, InteracaoLegacyRepository interacaoRepo) {
        this.legacyRepo = legacyRepo;
        this.ticketsRepo = ticketsRepo;
        this.mapRepo = mapRepo;
        this.issuesRepo = issuesRepo;
        this.interacaoRepo = interacaoRepo;
    }


    public void migrate() {
        Map<Long, Instant> firstResponses = new HashMap<>();

        for (InteracaoLegacy interacao : interacaoRepo.findPublicResponses()) {
            firstResponses.putIfAbsent(
                    interacao.getNrChamado(),
                    interacao.getDtInteracao()
            );
        }

        for (ChamadoLegacy legacy : legacyRepo.findAll()) {
            Long legacyId = legacy.getNrChamado();
            Instant firstResponseAt = firstResponses.get(legacyId);

            if (mapRepo.exists("ticket", legacyId)) {
                if (firstResponseAt != null) {
                    ticketsRepo.updateFirstResponse(legacyId, firstResponseAt);
                }
                continue;
            }

            UUID requesterId = mappedUuid("users",
                    legacy.getCdSolicitante());

            if (requesterId == null) {
                issue(legacyId, "TICKET_REQUESTER_NOT_MAPPED",
                        "Solicitante sem mapeamento; chamado não migrado.");
                continue;
            }

            UUID assigneeId = mappedUuid("users",
                    legacy.getCdTecnico());

            Long categoryId = mappedLong("category",
                    legacy.getCdCategoria());

            if (categoryId == null) {
                categoryId = mappedLongByName("Outros");
            }

            if (categoryId == null) {
                issue(legacyId, "TICKET_CATEGORY_NOT_FOUND",
                        "Categoria Outros não foi encontrada.");
                continue;
            }

            Instant openedAt = openedAt(legacy);
            Instant legacyClosedAt = validClosedAt(legacy, openedAt);

            String priority = priority(legacy.getIdPrioridade());
            String status = status(legacy.getStChamado(),
                    legacyClosedAt != null, assigneeId != null);

            String title = cleanText(legacy.getDsTitulo());
            String description = cleanText(legacy.getDsChamado());

            if (title.isBlank()) {
                title = description.substring(
                        0, Math.min(80, description.length()));
            }

            if (description.isBlank()) {
                description = title;
            }

            if (title.length() > 120) {
                title = title.substring(0, 120);
            }

            Instant resolvedAt = null;
            Instant closedAt = null;

            if ("resolved".equals(status)) {
                resolvedAt = legacyClosedAt != null
                        ? legacyClosedAt : openedAt;
            } else if ("closed".equals(status)
                    || "canceled".equals(status)) {
                closedAt = legacyClosedAt != null
                        ? legacyClosedAt : openedAt;

                if ("closed".equals(status)) {
                    resolvedAt = closedAt;
                }
            }

            long slaMinutes = 4320L;
            Instant dueAt = openedAt.plusSeconds(
                    (long) (slaMinutes * factor(priority) * 60));

            Boolean slaBreached = calculateSla(
                    status, resolvedAt, dueAt);

            Long score = null;
            String satisfactionComment = null;

            Integer grade = legacy.getNrNotaAvaliacao() == null
                    ? null : legacy.getNrNotaAvaliacao().intValue();

            if (("resolved".equals(status) || "closed".equals(status))
                    && grade != null && grade >= 0 && grade <= 10) {
                score = Math.max(1L, (long) Math.ceil(grade / 2.0));
                satisfactionComment = cleanText(legacy.getDsAvaliacao());
            } else if (grade != null || legacy.getDsAvaliacao() != null) {
                issue(legacyId, "TICKET_INVALID_SATISFACTION",
                        "Avaliação descartada por status ou nota inválida.");
            }

            Instant now = Instant.now();

            Ticket ticket = new Ticket(
                    UUID.randomUUID(),
                    String.format("HD-%04d-%06d",
                            openedAt.atZone(ZONE).getYear(), legacyId),
                    title,
                    description,
                    requesterId,
                    assigneeId,
                    categoryId,
                    priority,
                    status,
                    openedAt,
                    firstResponseAt,
                    resolvedAt,
                    closedAt,
                    dueAt,
                    slaBreached,
                    score,
                    satisfactionComment,
                    now,
                    now,
                    legacyId
            );

            UUID newId = ticketsRepo.save(ticket);

            if (newId == null) {
                newId = ticketsRepo.findByLegacyId(legacyId);
            }

            mapRepo.save(new MigrationIdMap(
                    "ticket", legacyId, newId.toString()));
        }
    }

    private UUID mappedUuid(String entity, Long legacyId) {
        if (legacyId == null || !mapRepo.exists(entity, legacyId)) {
            return null;
        }
        return UUID.fromString(
                mapRepo.findNewId(entity, legacyId).toString());
    }

    private Long mappedLong(String entity, Long legacyId) {
        if (legacyId == null || !mapRepo.exists(entity, legacyId)) {
            return null;
        }
        return Long.valueOf(
                mapRepo.findNewId(entity, legacyId).toString());
    }

    private Long mappedLongByName(String name) {
        // Substituir pela consulta de categoria por nome
        // no CategoriesRepository.
        return null;
    }

    private Instant openedAt(ChamadoLegacy c) {
        LocalDate date = c.getDtAbertura()
                .atZone(ZONE).toLocalDate();

        LocalTime time;
        try {
            time = LocalTime.parse(c.getHrAbertura());
        } catch (Exception e) {
            time = LocalTime.MIDNIGHT;
        }

        return LocalDateTime.of(date, time)
                .atZone(ZONE).toInstant();
    }

    private Instant validClosedAt(ChamadoLegacy c, Instant openedAt) {
        Instant closed = c.getDtFechadamento();
        return closed != null && !closed.isBefore(openedAt)
                ? closed : null;
    }

    private String priority(Long id) {
        if (id == null) return "medium";
        return switch (id.intValue()) {
            case 1 -> "low";
            case 2 -> "medium";
            case 3 -> "high";
            case 4 -> "critical";
            default -> "medium";
        };
    }

    private String status(String raw, boolean hasClosedDate,
                          boolean hasAssignee) {
        String code = raw == null ? "" : raw.trim().toUpperCase();

        String result = switch (code) {
            case "AB" -> "open";
            case "EA" -> "in_progress";
            case "AG" -> "waiting_customer";
            case "RS" -> "resolved";
            case "FC" -> "closed";
            case "CA" -> "canceled";
            default -> hasClosedDate ? "closed" : "open";
        };

        if (("in_progress".equals(result)
                || "waiting_customer".equals(result))
                && !hasAssignee) {
            return "open";
        }
        return result;
    }

    private double factor(String priority) {
        return switch (priority) {
            case "low" -> 2.0;
            case "high" -> 0.5;
            case "critical" -> 0.25;
            default -> 1.0;
        };
    }

    private Boolean calculateSla(String status, Instant resolvedAt,
                                 Instant dueAt) {
        if ("canceled".equals(status)) return null;

        if ("resolved".equals(status) || "closed".equals(status)) {
            return resolvedAt != null && resolvedAt.isAfter(dueAt);
        }

        return dueAt.isBefore(BACKUP_AT);
    }

    private String cleanText(String value) {
        if (value == null) return "";

        return value
                .replace("\r\n", "\n")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&aacute;", "á")
                .replace("&atilde;", "ã")
                .replace("&ccedil;", "ç")
                .replace("&eacute;", "é")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .trim();
    }

    private void issue(Long legacyId, String code, String message) {
        MigrationIssues issue = new MigrationIssues();
        issue.setEntity("ticket");
        issue.setLegacyId(legacyId);
        issue.setSeverity("warning");
        issue.setCode(code);
        issue.setMessage(message);
        issue.setCreatedAt(Instant.now());
        issuesRepo.save(issue);
    }
}

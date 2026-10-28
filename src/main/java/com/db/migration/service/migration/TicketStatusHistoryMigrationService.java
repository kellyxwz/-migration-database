package com.db.migration.service.migration;

import com.db.migration.model.legacy.InteracaoLegacy;
import com.db.migration.model.target.TicketStatusHistory;
import com.db.migration.repository.legacy.InteracaoLegacyRepository;
import com.db.migration.repository.target.TicketStatusHistoryRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TicketStatusHistoryMigrationService {

    private final InteracaoLegacyRepository interacaoRepo;
    private final TicketStatusHistoryRepository historyRepo;
    private final MigrationIdMapRepository mapRepo;

    private static final Pattern STATUS_PATTERN = Pattern.compile(
            "(?i)(?:STATUS:\\s*|Status alterado de\\s+)([A-Z]{2})\\s+para\\s+([A-Z]{2})|STATUS:\\s*([A-Z]{2})\\s*->\\s*([A-Z]{2})"
    );

    public TicketStatusHistoryMigrationService(
            InteracaoLegacyRepository interacaoRepo,
            TicketStatusHistoryRepository historyRepo,
            MigrationIdMapRepository mapRepo) {
        this.interacaoRepo = interacaoRepo;
        this.historyRepo = historyRepo;
        this.mapRepo = mapRepo;
    }

    public void migrate() {
        for (InteracaoLegacy legacy : interacaoRepo.findAll()) {
            Long legacyId = legacy.getCdInteracao();

            if (historyRepo.existsByLegacyInteractionId(legacyId)) {
                continue;
            }

            String body = legacy.getDsInteracao();

            if (body == null || body.isBlank()) {
                continue;
            }

            Matcher matcher = STATUS_PATTERN.matcher(body);

            if (!matcher.find()) {
                continue;
            }

            String fromStatus = matcher.group(1) != null
                    ? matcher.group(1)
                    : matcher.group(3);

            String toStatus = matcher.group(2) != null
                    ? matcher.group(2)
                    : matcher.group(4);

            UUID ticketId = mappedUuid("ticket", legacy.getNrChamado());

            if (ticketId == null) {
                continue;
            }

            String mappedFromStatus = mapStatus(fromStatus);
            String mappedToStatus = mapStatus(toStatus);

            if (mappedFromStatus == null || mappedToStatus == null) {
                continue;
            }

            TicketStatusHistory history = new TicketStatusHistory(
                    null,
                    ticketId,
                    mappedFromStatus,
                    mappedToStatus,
                    mappedUuid("users", legacy.getCd_usuario()),
                    legacy.getDtInteracao(),
                    legacyId
            );

            historyRepo.save(history);
        }
    }

    private UUID mappedUuid(String entity, Long legacyId) {
        if (legacyId == null || !mapRepo.exists(entity, legacyId)) {
            return null;
        }

        Object newId = mapRepo.findNewId(entity, legacyId, UUID.class);

        if (newId instanceof UUID uuid) {
            return uuid;
        }

        return UUID.fromString(newId.toString());
    }

    private String mapStatus(String status) {
        return switch (status.toUpperCase()) {
            case "AB" -> "open";
            case "EA" -> "in_progress";
            case "RE" -> "resolved";
            case "FE" -> "closed";
            case "CA" -> "canceled";
            default -> null;
        };
    }
}
package com.db.migration.service.migration;

import com.db.migration.model.legacy.InteracaoLegacy;
import com.db.migration.model.target.TicketComments;
import com.db.migration.repository.legacy.InteracaoLegacyRepository;
import com.db.migration.repository.target.TicketsCommentRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketCommentsMigrationService {

    private final InteracaoLegacyRepository interacaoRepo;
    private final TicketsCommentRepository commentsRepo;
    private final MigrationIdMapRepository mapRepo;

    public TicketCommentsMigrationService(
            InteracaoLegacyRepository interacaoRepo,
            TicketsCommentRepository commentsRepo,
            MigrationIdMapRepository mapRepo) {
        this.interacaoRepo = interacaoRepo;
        this.commentsRepo = commentsRepo;
        this.mapRepo = mapRepo;
    }

    public void migrate() {
        for (InteracaoLegacy legacy : interacaoRepo.findAll()) {
            Long legacyId = legacy.getCdInteracao();

            if (commentsRepo.existsByLegacyId(legacyId)) {
                continue;
            }

            UUID ticketId = mappedUuid(
                    "ticket", legacy.getNrChamado());

            if (ticketId == null) {
                continue;
            }

            Long legacyUserId = legacy.getCd_usuario();

            if (legacyUserId == null || legacyUserId == 0) {
                continue;
            }

            UUID authorId = mappedUuid("users", legacyUserId);

            if (authorId == null) {
                continue;
            }


            String visibility = "S".equals(legacy.getFlIntercao())
                    ? "internal" : "public";

            String body = legacy.getDsInteracao();

            if (body == null || body.isBlank()) {
                continue;
            }

            TicketComments comment = new TicketComments(
                    null,
                    ticketId,
                    authorId,
                    body,
                    visibility,
                    legacy.getDtInteracao(),
                    legacyId
            );

            commentsRepo.save(comment);
        }
    }

    private UUID mappedUuid(String entity, Long legacyId) {
        if (legacyId == null || !mapRepo.exists(entity, legacyId)) {
            return null;
        }

        Object id = mapRepo.findNewId(entity, legacyId, UUID.class);

        return id != null ? UUID.fromString(id.toString()) : null;
    }
}
package com.db.migration.service.migration;

import com.db.migration.model.legacy.ChamadoLegacy;
import com.db.migration.model.target.TicketTags;
import com.db.migration.repository.legacy.ChamadoLegacyRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.TagsRepository;
import com.db.migration.repository.target.TicketTagsRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TicketTagsMigrationService {

    private final ChamadoLegacyRepository chamadoRepo;
    private final TicketTagsRepository ticketTagsRepo;
    private final TagsRepository tagsRepo;
    private final MigrationIdMapRepository mapRepo;

    public TicketTagsMigrationService(
            ChamadoLegacyRepository chamadoRepo,
            TicketTagsRepository ticketTagsRepo,
            TagsRepository tagsRepo,
            MigrationIdMapRepository mapRepo) {
        this.chamadoRepo = chamadoRepo;
        this.ticketTagsRepo = ticketTagsRepo;
        this.tagsRepo = tagsRepo;
        this.mapRepo = mapRepo;
    }

    public void migrate() {
        var chamados = chamadoRepo.findAll();
        System.out.println("Total de chamados: " + chamados.size());

        for (ChamadoLegacy chamado : chamados) {
            String tags = chamado.getDsTags();

            if (tags == null || tags.isBlank()) {
                continue;
            }

            System.out.println("Chamado: " + chamado.getNrChamado());
            System.out.println("Tags legadas: " + tags);

            UUID ticketId = mapRepo.findMappedUuid(
                    "ticket", chamado.getNrChamado()
            );

            if (ticketId == null) {
                System.out.println("Ticket sem mapeamento");
                continue;
            }

            for (String nome : tags.split(",")) {
                nome = nome.trim();

                if (nome.isBlank()) {
                    continue;
                }

                Long tagId = tagsRepo.findIdByName(nome);

                if (tagId == null) {
                    System.out.println("Tag não encontrada: " + nome);
                    continue;
                }

                ticketTagsRepo.save(new TicketTags(ticketId, tagId));
                System.out.println("Relacionamento salvo: " + nome);
            }
        }
    }

}
package com.db.migration.service.migration;

import com.db.migration.model.target.Tag;
import com.db.migration.repository.legacy.ChamadoLegacyRepository;
import com.db.migration.repository.target.TagsRepository;
import org.springframework.stereotype.Service;

@Service
public class TagsMigrationService {

    private final ChamadoLegacyRepository chamadoRepo;
    private final TagsRepository tagsRepo;

    public TagsMigrationService(
            ChamadoLegacyRepository chamadoRepo,
            TagsRepository tagsRepo) {
        this.chamadoRepo = chamadoRepo;
        this.tagsRepo = tagsRepo;
    }

    public void migrate() {
        var chamados = chamadoRepo.findAll();

        for (var chamado : chamados) {
            String tags = chamado.getDsTags();

            if (tags == null || tags.isBlank()) {
                continue;
            }

            for (String nome : tags.split(",")) {
                nome = nome.trim();

                if (nome.isBlank()) {
                    continue;
                }

                if (tagsRepo.findIdByName(nome) == null) {
                    tagsRepo.save(new Tag(null, nome));
                    System.out.println("Tag criada: " + nome);
                }
            }
        }
    }
}
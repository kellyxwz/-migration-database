package com.db.migration.service.migration;

import com.db.migration.model.target.Tag;
import com.db.migration.repository.legacy.ChamadoLegacyRepository;
import com.db.migration.repository.target.TagsRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

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

            for (String nome : tags.split("[,;]")) {
                nome = normalize(nome);

                if (nome.isBlank()) {
                    continue;
                }

                if (tagsRepo.findIdByName(nome) == null) {
                    tagsRepo.save(new Tag(null, nome));
                }
            }
        }
    }

    private String normalize(String nome) {
        return Normalizer.normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
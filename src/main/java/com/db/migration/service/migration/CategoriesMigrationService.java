package com.db.migration.service.migration;

import com.db.migration.model.legacy.CategoriaLegacy;
import com.db.migration.model.target.Categories;
import com.db.migration.model.target.MigrationIdMap;
import com.db.migration.repository.legacy.CategoriaLegacyRepository;
import com.db.migration.repository.target.CategoriesRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.MigrationIssuesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class CategoriesMigrationService {

    private final CategoriaLegacyRepository legacyRepo;
    private final CategoriesRepository categoriesRepo;
    private final MigrationIdMapRepository mapRepo;
    private final MigrationIssuesRepository issuesRepo;

    public CategoriesMigrationService(
            CategoriaLegacyRepository legacyRepo,
            CategoriesRepository categoriesRepo,
            MigrationIdMapRepository mapRepo,
            MigrationIssuesRepository issuesRepo) {
        this.legacyRepo = legacyRepo;
        this.categoriesRepo = categoriesRepo;
        this.mapRepo = mapRepo;
        this.issuesRepo = issuesRepo;
    }

    @Transactional
    public void migrate() {
        List<CategoriaLegacy> all = legacyRepo.findAll();
        Map<Long, CategoriaLegacy> byId = new HashMap<>();
        all.forEach(c -> byId.put(c.getCdCategoria(), c));

        Set<Long> done = new HashSet<>();
        Map<String, Long> canonical = new HashMap<>();

        while (done.size() < all.size()) {
            boolean progress = false;

            for (CategoriaLegacy c : all) {
                Long id = c.getCdCategoria();
                if (done.contains(id)) continue;

                if (mapRepo.exists("category", id)) {
                    done.add(id);
                    progress = true;
                    continue;
                }

                Long parentLegacy = c.getCdCategoriaPai();

                if (parentLegacy != null && !byId.containsKey(parentLegacy)) {
                    issue(id, "CATEGORY_PARENT_NOT_FOUND",
                            "Pai inexistente; categoria migrada como raiz.");
                    parentLegacy = null;
                }

                if (parentLegacy != null && !mapRepo.exists("category", parentLegacy))
                    continue;

                Long parentNew = parentLegacy == null ? null
                        : mappedId(parentLegacy);

                String key = norm(c.getDcCategoria()).toLowerCase()
                        + "|" + Objects.toString(parentLegacy, "root");

                if (canonical.containsKey(key)) {
                    Long canonicalLegacyId = canonical.get(key);

                    String canonicalNewId = String.valueOf(
                            mappedId(canonicalLegacyId)
                    );

                    mapRepo.save(new MigrationIdMap(
                            "category", id, canonicalNewId
                    ));

                    issue(id, "CATEGORY_DUPLICATE_MERGED",
                            "Categoria unificada com código legado " + canonicalLegacyId);
                } else {
                    Categories target = new Categories();
                    target.setName(norm(c.getDcCategoria()));
                    target.setParentId(parentNew);
                    target.setResolutionSlaMinutes(sla(c, byId, new HashSet<>()));
                    target.setActive(!"N".equalsIgnoreCase(c.getFlAtivo()));
                    target.setLegacyId(id);

                    Long newId = categoriesRepo.save(target);

                    mapRepo.save(new MigrationIdMap(
                            "category", id, String.valueOf(newId)
                    ));

                    canonical.put(key, id);
                }

                done.add(id);
                progress = true;
            }

            if (!progress) {
                for (CategoriaLegacy c : all) {
                    Long id = c.getCdCategoria();

                    if (!done.contains(id)) {
                        issue(id, "CATEGORY_HIERARCHY_INVALID",
                                "Hierarquia não resolvida; migrada como raiz.");

                        c.setCdCategoriaPai(null);
                    }
                }

                progress = true;
            }
        }
    }

    private Long sla(CategoriaLegacy c, Map<Long, CategoriaLegacy> byId,
                     Set<Long> visited) {
        if (!visited.add(c.getCdCategoria())) return 4320L;

        if (c.getNrSlaHoras() != null && c.getNrSlaHoras() > 0)
            return c.getNrSlaHoras() * 60L;

        CategoriaLegacy parent = byId.get(c.getCdCategoriaPai());
        return parent == null ? 4320L : sla(parent, byId, visited);
    }

    private Long mappedId(Long legacyId) {
        return Long.valueOf(
                mapRepo.findNewId("category", legacyId).toString()
        );
    }

    private String norm(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private void issue(Long id, String code, String message) {
        var issue = new com.db.migration.model.target.MigrationIssues();
        issue.setEntity("category");
        issue.setLegacyId(id);
        issue.setSeverity("warning");
        issue.setCode(code);
        issue.setMessage(message);
        issue.setCreatedAt(Instant.now());

        issuesRepo.save(issue);
    }
}

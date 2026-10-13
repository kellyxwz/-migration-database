package com.db.migration.service.migration;

import com.db.migration.model.legacy.SetorLegacy;
import com.db.migration.model.target.Department;
import com.db.migration.repository.legacy.SetorLegacyRepository;
import com.db.migration.repository.target.DepartmentRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentMigrationService {

    private final SetorLegacyRepository setorLegacyRepository;
    private final DepartmentRepository departmentRepository;
    private final MigrationIdMapRepository migrationIdMapRepository;

    public DepartmentMigrationService(
            SetorLegacyRepository setorLegacyRepository,
            DepartmentRepository departmentRepository,
            MigrationIdMapRepository migrationIdMapRepository) {

        this.setorLegacyRepository = setorLegacyRepository;
        this.departmentRepository = departmentRepository;
        this.migrationIdMapRepository = migrationIdMapRepository;
    }

    public void migrate() {

        List<SetorLegacy> setores = setorLegacyRepository.findAll();

        Map<String, List<SetorLegacy>> grupos = setores.stream()
                .collect(Collectors.groupingBy(
                        setor -> normalizeName(setor.getNmSetor())
                ));

        for (List<SetorLegacy> grupo : grupos.values()) {


            SetorLegacy principal = grupo.stream()
                    .min(Comparator.comparing(SetorLegacy::getCdSetor))
                    .orElseThrow();

            if (migrationIdMapRepository.exists(
                    "department",
                    (long) principal.getCdSetor())) {

                continue;
            }

            String nome = normalizeName(principal.getNmSetor());

            boolean ativo = !"N".equals(principal.getFlAtivo());

            Long newId = departmentRepository.findIdByName(nome);

            if (newId == null) {

                Department department = new Department(
                        nome,
                        ativo,
                        principal.getCdSetor(),
                        Instant.now()
                );

                newId = departmentRepository.save(department);
            }

            MigrationIdMap migration = new MigrationIdMap(
                    "department",
                    principal.getCdSetor(),
                    String.valueOf(newId)
            );

            migrationIdMapRepository.save(migration);
        }
    }

    private String normalizeName(String nome) {

        if (nome == null) {
            return null;
        }

        return nome
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

}

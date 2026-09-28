package com.db.migration.service.migration;


import com.db.migration.model.legacy.SetorLegacy;
import com.db.migration.model.target.Department;
import com.db.migration.repository.legacy.SetorLegacyRepository;
import com.db.migration.repository.target.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentMigrationService {

    private final SetorLegacyRepository setorLegacyRepository;
    private final DepartmentRepository departmentRepository;

    public DepartmentMigrationService(
            SetorLegacyRepository setorLegacyRepository,
            DepartmentRepository departmentRepository) {

        this.setorLegacyRepository = setorLegacyRepository;
        this.departmentRepository = departmentRepository;
    }

    public void migrate() {

        List<SetorLegacy> setores = setorLegacyRepository.findAll();

        Map<String, List<SetorLegacy>> grupos = setores.stream()
                .collect(Collectors.groupingBy(
                        setor -> normalizeName(setor.getNmSetor())
                ));

        for (List<SetorLegacy> grupo : grupos.values()) {

            SetorLegacy principal = grupo.stream()
                    .min((a, b) -> Integer.compare(
                            a.getCdSetor(),
                            b.getCdSetor()
                    ))
                    .orElseThrow();

            String nome = normalizeName(principal.getNmSetor());

            boolean ativo = !"N".equals(principal.getFlAtivo());

            Department department = new Department(
                    nome,
                    ativo,
                    principal.getCdSetor(),
                    Instant.now()
            );

            departmentRepository.save(department);
        }
    }

    private String normalizeName(String nome) {

        if (nome == null) {
            return null;
        }

        return nome.trim().replaceAll("\\s+", " ");
    }

}

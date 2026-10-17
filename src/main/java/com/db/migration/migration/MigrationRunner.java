package com.db.migration.migration;

import com.db.migration.service.migration.DepartmentMigrationService;
import com.db.migration.service.migration.UsersMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private final UsersMigrationService usersMigrationService;
    public final DepartmentMigrationService departmentMigrationService;

    public MigrationRunner(UsersMigrationService usersMigrationService, DepartmentMigrationService departmentMigrationService) {
        this.usersMigrationService = usersMigrationService;
        this.departmentMigrationService = departmentMigrationService;
    }

    @Override
    public void run(String... args) {
        usersMigrationService.migrateUsers();
        departmentMigrationService.migrate();
    }

}

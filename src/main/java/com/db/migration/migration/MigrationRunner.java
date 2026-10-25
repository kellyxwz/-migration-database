package com.db.migration.migration;

import com.db.migration.service.migration.CategoriesMigrationService;
import com.db.migration.service.migration.DepartmentMigrationService;
import com.db.migration.service.migration.UsersMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private final TruncateDatabase truncateDatabase;
    private final UsersMigrationService usersMigrationService;
    public final DepartmentMigrationService departmentMigrationService;
    private  final CategoriesMigrationService categoriesMigrationService;

    public MigrationRunner(TruncateDatabase truncateDatabase, UsersMigrationService usersMigrationService, DepartmentMigrationService departmentMigrationService, CategoriesMigrationService categoriesMigrationService) {
        this.truncateDatabase = truncateDatabase;
        this.usersMigrationService = usersMigrationService;
        this.departmentMigrationService = departmentMigrationService;
        this.categoriesMigrationService = categoriesMigrationService;
    }

    @Override
    public void run(String... args) {
        truncateDatabase.deleteAllData();
        departmentMigrationService.migrate();
        usersMigrationService.migrateUsers();
        categoriesMigrationService.migrate();
    }

}

package com.db.migration.migration;

import com.db.migration.service.migration.CategoriesMigrationService;
import com.db.migration.service.migration.DepartmentMigrationService;
import com.db.migration.service.migration.TicketsMigrationService;
import com.db.migration.service.migration.UsersMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private final TruncateDatabase truncateDatabase;
    private final UsersMigrationService usersMigrationService;
    private final DepartmentMigrationService departmentMigrationService;
    private final CategoriesMigrationService categoriesMigrationService;
    private final TicketsMigrationService ticketsMigrationService;



    public MigrationRunner(TruncateDatabase truncateDatabase, UsersMigrationService usersMigrationService, DepartmentMigrationService departmentMigrationService, CategoriesMigrationService categoriesMigrationService, TicketsMigrationService ticketsMigrationService) {
        this.truncateDatabase = truncateDatabase;
        this.usersMigrationService = usersMigrationService;
        this.departmentMigrationService = departmentMigrationService;
        this.categoriesMigrationService = categoriesMigrationService;
        this.ticketsMigrationService = ticketsMigrationService;
    }

    @Override
    public void run(String... args) {
        truncateDatabase.deleteAllData();
        departmentMigrationService.migrate();
        usersMigrationService.migrateUsers();
        categoriesMigrationService.migrate();
        ticketsMigrationService.migrate();
    }

}

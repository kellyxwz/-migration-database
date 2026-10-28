package com.db.migration.migration;

import com.db.migration.service.migration.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private final TruncateDatabase truncateDatabase;
    private final UsersMigrationService usersMigrationService;
    private final DepartmentMigrationService departmentMigrationService;
    private final CategoriesMigrationService categoriesMigrationService;
    private final TicketsMigrationService ticketsMigrationService;
    private final TicketCommentsMigrationService ticketCommentsMigrationService;
    private final TicketStatusHistoryMigrationService ticketStatusHistoryMigrationService;

    public MigrationRunner(TruncateDatabase truncateDatabase, UsersMigrationService usersMigrationService, DepartmentMigrationService departmentMigrationService, CategoriesMigrationService categoriesMigrationService, TicketsMigrationService ticketsMigrationService, TicketCommentsMigrationService ticketCommentsMigrationService, TicketStatusHistoryMigrationService ticketStatusHistoryMigrationService) {
        this.truncateDatabase = truncateDatabase;
        this.usersMigrationService = usersMigrationService;
        this.departmentMigrationService = departmentMigrationService;
        this.categoriesMigrationService = categoriesMigrationService;
        this.ticketsMigrationService = ticketsMigrationService;
        this.ticketCommentsMigrationService = ticketCommentsMigrationService;
        this.ticketStatusHistoryMigrationService = ticketStatusHistoryMigrationService;
    }

    @Override
    public void run(String... args) {
        truncateDatabase.deleteAllData();
        departmentMigrationService.migrate();
        usersMigrationService.migrateUsers();
        categoriesMigrationService.migrate();
        ticketsMigrationService.migrate();
        ticketCommentsMigrationService.migrate();
        System.out.println("Starting ticket status history migration...");
        ticketStatusHistoryMigrationService.migrate();
    }

}

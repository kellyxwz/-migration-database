package com.db.migration.service.migration;

import com.db.migration.model.target.Users;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.UsersRepository;
import org.springframework.stereotype.Service;

@Service
public class UsersMigrationService {

    private final UsersRepository usersRepository;
    private final MigrationIdMapRepository migrationIdMapRepository;

    public UsersMigrationService(UsersRepository usersRepository, MigrationIdMapRepository migrationIdMapRepository) {
        this.usersRepository = usersRepository;
        this.migrationIdMapRepository = migrationIdMapRepository;
    }
}

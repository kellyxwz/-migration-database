package com.db.migration.repository.target;

import com.db.migration.model.target.Users;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.UUID;

@Repository
public class UsersRepository {

    private final JdbcTemplate jdbc;

    public UsersRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public UUID save(Users users) {
        String sql = """
                INSERT INTO users (first_name, last_name, email,
                 phone, department_id, role, status, password_hash,
                 must_reset_password, created_at, updated_at, legacy_id)
                 VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        return jdbc.queryForObject(
                sql,
                UUID.class,
                users.getFirstName(),
                users.getLastName(),
                users.getEmail(),
                users.getPhone(),
                users.getDepartmentId(),
                users.getRole(),
                users.getStatus(),
                users.getPasswordHash(),
                users.isMustResetPassword(),
                users.getCreatedAt() == null ? null
                        : Timestamp.from(users.getCreatedAt()),
                users.getUpdateAt() == null ? null
                        : Timestamp.from(users.getUpdateAt()),
                users.getLegacyId()
                );


    }
}

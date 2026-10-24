package com.db.migration.repository.target;

import com.db.migration.model.target.MigrationIssues;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;

@Repository
public class MigrationIssuesRepository {


    private final JdbcTemplate jdbcTemplate;

    public MigrationIssuesRepository(@Qualifier("newDataSource")DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save (MigrationIssues migrationIssues){

        String sql = """
            INSERT INTO migration_issues(
            entity, legacy_id, severity, code, message, created_At)
            VALUES(?, ?, ?::issue_severity, ?, ?, ?)
            """;

        jdbcTemplate.update(
                sql,
                migrationIssues.getEntity(),
                migrationIssues.getLegacyId(),
                migrationIssues.getSeverity(),
                migrationIssues.getCode(),
                migrationIssues.getMessage(),
                Timestamp.from(migrationIssues.getCreatedAt())
                );

    }

    public boolean exists(String entity, Long legacyId){
        String sql = """
                SELECT EXISTS (
                SELECT 1
                FROM migration_issues
                WHERE entity = ?
                AND legacy_id = ?
                """;

        return  Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        entity,
                        legacyId
                )
        );
    }

}

package com.db.migration.repository.target;

import com.db.migration.model.target.MigrationIdMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MigrationIdMapRepository {

    private final JdbcTemplate jdbcTemplate;


    public MigrationIdMapRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(MigrationIdMap migration){
        String sql = """
                INSERT INTO migration_id_map
                (entity, legacy_id, new_id)
                VALUES (?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                migration.getEntity(),
                migration.getLegacyId(),
                migration.getNewId()
        );
    }

    public Object findNewId(String entity, Long legacyId) {
        String sql = """
            SELECT new_id
            FROM migration_id_map
            WHERE entity = ?
              AND legacy_id = ?
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Object.class,
                entity,
                legacyId
        );
    }

    public boolean exists(String entity, Long legacyId) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM migration_id_map
                WHERE entity = ?
                AND legacy_id = ?
            )
            """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        entity,
                        legacyId
                )
        );
    }


    public Optional<Long> findDepartmentNewId( String entity,Long legacyId) {
        String sql = """
            SELECT new_id
            FROM migration_id_map
            WHERE entity = 'department'
              AND legacy_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                rs -> rs.next()
                        ? Optional.of(rs.getLong("new_id"))
                        : Optional.empty(),
                legacyId
        );
    }
}

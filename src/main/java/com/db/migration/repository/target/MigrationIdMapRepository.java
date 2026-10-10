package com.db.migration.repository.target;

import com.db.migration.service.migration.MigrationIdMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

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


}

package com.db.migration.repository.target;

import com.db.migration.model.legacy.CategoriaLegacy;
import com.db.migration.model.target.Categories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class CategoriesRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoriesRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long save(Categories categories){

        String sql = """
                INSERT INTO categories (name, parent_id,
                resolution_sla_minutes, is_active, legacy_id)
                VALUES (?,?,?,?,?)
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                categories.getName(),
                categories.getParentId(),
                categories.getResolutionSlaMinutes(),
                categories.isActive(),
                categories.getLegacyId()
        );

    }

}

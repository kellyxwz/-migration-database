package com.db.migration.repository.target;

import com.db.migration.model.target.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class TagsRepository {

    private final JdbcTemplate jdbcTemplate;

    public TagsRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long save(Tag tag) {
        String sql = """
                INSERT INTO tags (name)
                VALUES (?)
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                tag.getName()
        );
    }

    public Long findIdByName(String name) {
        String sql = """
                SELECT id
                FROM tags
                WHERE LOWER(name) = LOWER(?)
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                rs -> rs.next() ? rs.getLong("id") : null,
                name
        );
    }
}
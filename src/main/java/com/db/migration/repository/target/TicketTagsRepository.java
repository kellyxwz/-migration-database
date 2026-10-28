package com.db.migration.repository.target;

import com.db.migration.model.target.TicketTags;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class TicketTagsRepository {

    private final JdbcTemplate jdbcTemplate;

    public TicketTagsRepository(
            @Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(TicketTags ticketTag) {
        String sql = """
                INSERT INTO ticket_tags (ticket_id, tag_id)
                VALUES (?, ?)
                ON CONFLICT (ticket_id, tag_id) DO NOTHING
                """;

        jdbcTemplate.update(
                sql,
                ticketTag.getTicketId(),
                ticketTag.getTagId()
        );
    }
}
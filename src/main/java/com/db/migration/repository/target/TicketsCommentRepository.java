package com.db.migration.repository.target;

import com.db.migration.model.target.TicketComments;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;


@Repository
public class TicketsCommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public TicketsCommentRepository(
            @Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long save(TicketComments comment) {
        String sql = """
            INSERT INTO ticket_comments (
                ticket_id,
                author_id,
                body,
                visibility,
                created_at,
                legacy_id
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                comment.getTicketId(),
                comment.getAuthorId(),
                comment.getBody(),
                comment.getVisibility(),
                comment.getCreatedAt() != null
                        ? Timestamp.from(comment.getCreatedAt()) : null,
                comment.getLegacyId()
        );
    }

    public boolean existsByLegacyId(Long legacyId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM ticket_comments
                WHERE legacy_id = ?
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql, Boolean.class, legacyId);

        return Boolean.TRUE.equals(exists);
    }
}
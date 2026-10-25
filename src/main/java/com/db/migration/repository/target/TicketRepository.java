package com.db.migration.repository.target;

import com.db.migration.model.target.Ticket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.UUID;

@Repository
public class TicketRepository {

    private final JdbcTemplate jdbcTemplate;

    public TicketRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UUID save(Ticket ticket) {
        String sql = """
            INSERT INTO tickets (
                id, code, title, description, requester_id, assignee_id,
                category_id, priority, status, opened_at, first_response_at,
                resolved_at, closed_at, due_at, sla_breached,
                satisfaction_score, satisfaction_comment, created_at,
                updated_at, legacy_id
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?::ticket_priority, ?::ticket_status,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )
            ON CONFLICT (legacy_id) DO NOTHING
            RETURNING id
            """;

        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setObject(1, ticket.getId());
                    ps.setString(2, ticket.getCode());
                    ps.setString(3, ticket.getTitle());
                    ps.setString(4, ticket.getDescription());
                    ps.setObject(5, ticket.getRequesterId());
                    ps.setObject(6, ticket.getAssigneeId());
                    ps.setObject(7, ticket.getCategoryId());
                    ps.setString(8, ticket.getPriority());
                    ps.setString(9, ticket.getStatus());
                    ps.setObject(10, ticket.getOpenedAt());
                    ps.setObject(11, ticket.getFirstResponseAt());
                    ps.setObject(12, ticket.getResolvedAt());
                    ps.setObject(13, ticket.getClosedAt());
                    ps.setObject(14, ticket.getDueAt());
                    ps.setObject(15, ticket.getSlaBreached());
                    ps.setObject(16, ticket.getSatisfactionScore());
                    ps.setString(17, ticket.getSatisfactionComment());
                    ps.setObject(18, ticket.getCreatedAt());
                    ps.setObject(19, ticket.getUpdatedAt());
                    ps.setObject(20, ticket.getLegacyId());
                },
                rs -> rs.next() ? rs.getObject("id", UUID.class) : null
        );
    }

    public boolean existsByLegacyId(Long legacyId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM tickets WHERE legacy_id = ?
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql, Boolean.class, legacyId
        );

        return Boolean.TRUE.equals(exists);
    }
}
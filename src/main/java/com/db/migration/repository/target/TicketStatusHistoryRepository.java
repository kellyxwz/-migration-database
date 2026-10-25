package com.db.migration.repository.target;

import com.db.migration.model.target.TicketStatusHistory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class TicketStatusHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TicketStatusHistoryRepository(
            @Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Long save(TicketStatusHistory history) {
        String sql = """
            INSERT INTO ticket_status_history (
                ticket_id,
                from_status,
                to_status,
                changed_by,
                legacy_interaction_id
            )
            VALUES (?, ?::ticket_status, ?::ticket_status, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                history.getTicketId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy(),
                history.getLegacyInteractionId()
        );
    }

    public boolean existsByLegacyInteractionId(Long legacyId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM ticket_status_history
                WHERE legacy_interaction_id = ?
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql, Boolean.class, legacyId);

        return Boolean.TRUE.equals(exists);
    }
}
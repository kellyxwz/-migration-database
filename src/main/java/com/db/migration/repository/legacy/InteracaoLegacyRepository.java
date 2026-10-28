package com.db.migration.repository.legacy;

import com.db.migration.model.legacy.InteracaoLegacy;
import com.db.migration.model.legacy.UserActivityDateLegacy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class InteracaoLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public InteracaoLegacyRepository(DataSource legacyDataSource) {
        this.jdbcTemplate = new JdbcTemplate(legacyDataSource);
    }

    public List<InteracaoLegacy> findAll() {
        String sql = """
            SELECT cd_interacao, nr_chamado, cd_usuario,
                   ds_interacao, dt_interacao, fl_interno
            FROM tb_interacao
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new InteracaoLegacy(
                        rs.getLong("cd_interacao"),
                        rs.getLong("nr_chamado"),
                        rs.getLong("cd_usuario"),
                        rs.getString("ds_interacao"),
                        rs.getTimestamp("dt_interacao") != null
                                ? rs.getTimestamp("dt_interacao").toInstant()
                                : null,
                        rs.getString("fl_interno")
                )
        );
    }

    public List<UserActivityDateLegacy> findUserActivityDates() {
        String sql = """
            SELECT cd_usuario, dt_interacao
            FROM tb_interacao
            WHERE cd_usuario IS NOT NULL
              AND dt_interacao IS NOT NULL
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UserActivityDateLegacy(
                        rs.getLong("cd_usuario"),
                        rs.getTimestamp("dt_interacao").toInstant()
                )
        );
    }

    public List<InteracaoLegacy> findPublicResponses() {
        String sql = """
        SELECT i.cd_interacao, i.nr_chamado, i.cd_usuario,
               i.ds_interacao, i.dt_interacao, i.fl_interno
        FROM tb_interacao i
        JOIN tb_usuario u ON u.cd_usuario = i.cd_usuario
        WHERE i.dt_interacao IS NOT NULL
          AND (i.fl_interno = 'N' OR i.fl_interno IS NULL)
          AND u.tp_perfil IN ('A', 'T')
        ORDER BY i.nr_chamado, i.dt_interacao, i.cd_interacao
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new InteracaoLegacy(
                        rs.getLong("cd_interacao"),
                        rs.getLong("nr_chamado"),
                        rs.getLong("cd_usuario"),
                        rs.getString("ds_interacao"),
                        rs.getTimestamp("dt_interacao").toInstant(),
                        rs.getString("fl_interno")
                )
        );
    }
}

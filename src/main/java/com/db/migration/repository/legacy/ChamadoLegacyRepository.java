package com.db.migration.repository.legacy;

import com.db.migration.model.legacy.ChamadoLegacy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ChamadoLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChamadoLegacyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ChamadoLegacy> findAll(){

        String sql = """
                SELECT nr_chamado, ds_titulo, ds_chamado, cd_solicitante,
                cd_tecnico, cd_categoria, id_prioridade, st_chamado, dt_abertura,
                hr_abertura, dt_fechamento, nr_nota_avaliacao,
                ds_avaliacao, fl_sla_estourado, ds_tags
                FROM  tb_chamado
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    new ChamadoLegacy(
                            rs.getLong("nr_chamado"),
                            rs.getString("ds_titulo"),
                            rs.getString("ds_chamado"),
                            rs.getLong("cd_solicitante"),
                            rs.getLong("cd_tecnico"),
                            rs.getLong("cd_categoria"),
                            rs.getLong("id_prioridade"),
                            rs.getString("st_chamado"),
                            rs.getTimestamp("dt_abertura")
                                    != null ? rs.getTimestamp
                                    ("dt_abertura").toInstant() : null,
                            rs.getString("hr_abertura"),
                            rs.getTimestamp("dt_fechamento")
                                    != null ? rs.getTimestamp
                                    ("dt_fechamento").toInstant() : null,
                            rs.getLong("nr_nota_avaliacao"),
                            rs.getString("ds_avaliacao"),
                            rs.getString("fl_sla_estourado"),
                            rs.getString("ds_tags")
                    )
            );

    }
}
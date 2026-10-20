package com.db.migration.repository.legacy;

import com.db.migration.model.legacy.CategoriaLegacy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class CategoriaLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaLegacyRepository(DataSource legacyDatasource) {
        this.jdbcTemplate = new JdbcTemplate(legacyDatasource);
    }

    List<CategoriaLegacy> findAll() {

        String sql = """
                SELECT cd_categoria, ds_categoria, cd_categoria_pai,
                nr_sla_horas, fl_ativo, dt_cadastro
                FROM tb_categoria
                """;

        return jdbcTemplate.query(
                sql, (rs, rowNum) ->
                        new CategoriaLegacy(
                                rs.getLong("cd_categoria"),
                                rs.getString("ds_categoria"),
                                rs.getLong("cd_categoria_pai"),
                                rs.getLong("nr_sla_horas"),
                                rs.getString("fl_ativo")
                        )

        );
    }

}

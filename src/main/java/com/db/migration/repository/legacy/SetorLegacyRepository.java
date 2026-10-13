package com.db.migration.repository.legacy;


import com.db.migration.model.legacy.SetorLegacy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SetorLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public SetorLegacyRepository(DataSource legacyDataSource) {
        this.jdbcTemplate = new JdbcTemplate(legacyDataSource);
    }

    public List<SetorLegacy> findAll() {

        String sql = "SELECT cd_setor, nm_setor, fl_ativo FROM tb_" + "setor";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new SetorLegacy(
                        rs.getInt("cd_setor"),
                        rs.getString("nm_setor"),
                        rs.getString("fl_ativo")
                )
        );
    }
}
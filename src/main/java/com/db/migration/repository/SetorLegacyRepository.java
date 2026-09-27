package com.db.migration.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class SetorLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public SetorLegacyRepository(DataSource legacyDataSource) {
        this.jdbcTemplate = new JdbcTemplate(legacyDataSource);
    }

    public void testarConsulta() {
        String sql = "SELECT * FROM tb_setor";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            System.out.println(rs.getInt("cd_setor"));
            System.out.println(rs.getString("nm_setor"));
            return null;
        });
    }
}
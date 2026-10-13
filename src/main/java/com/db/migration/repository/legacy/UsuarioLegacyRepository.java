package com.db.migration.repository.legacy;

import com.db.migration.model.legacy.UsuarioLegacy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UsuarioLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioLegacyRepository(DataSource legacyDataSource) {
        this.jdbcTemplate = new JdbcTemplate(legacyDataSource);
    }

    public List<UsuarioLegacy> findAll() {
        String sql = """ 
                SELECT cd_usuario, nm_usuario, ds_email, nr_telefone, 
                cd_setor, tp_perfil, ds_senha, dt_cadastro, fl_ativo 
                FROM tb_usuario 
                """;

        return jdbcTemplate.query
                (sql, (rs, rowNum) ->
                        new UsuarioLegacy( rs.getInt("cd_usuario"),
                                rs.getString("nm_usuario"),
                                rs.getString("ds_email"),
                                rs.getString("nr_telefone"),
                                rs.getInt("cd_setor"),
                                rs.getString("tp_perfil"),
                                rs.getString("ds_senha"),
                                rs.getString("dt_cadastro"),
                                rs.getString("fl_ativo")
                        )
                );
    }
}

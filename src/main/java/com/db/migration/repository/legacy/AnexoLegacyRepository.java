package com.db.migration.repository.legacy;

import com.db.migration.model.legacy.AnexoLegacy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class AnexoLegacyRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnexoLegacyRepository(
            @Qualifier("legacyDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<AnexoLegacy> findAll() {
        String sql = """
                SELECT cd_anexo, nr_chamado, nm_arquivo,
                       ds_caminho, nr_tamanho_kb, dt_upload
                FROM tb_anexo
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AnexoLegacy anexo = new AnexoLegacy();
            anexo.setCdAnexo(rs.getLong("cd_anexo"));
            anexo.setNrChamado(rs.getLong("nr_chamado"));
            anexo.setNmArquivo(rs.getString("nm_arquivo"));
            anexo.setDsCaminho(rs.getString("ds_caminho"));
            anexo.setNrTamanhoKb(rs.getLong("nr_tamanho_kb"));

            Timestamp upload = rs.getTimestamp("dt_upload");
            anexo.setDtUpload(
                    upload != null ? upload.toInstant() : null
            );

            return anexo;
        });
    }
}
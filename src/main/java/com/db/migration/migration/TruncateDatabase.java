package com.db.migration.migration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TruncateDatabase {

    private final JdbcTemplate jdbcTemplate;

    public TruncateDatabase(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void deleteAllData() {

        String sql = """
                DO $$\s
                DECLARE\s
                    _tbl text;\s
                    _sch text;\s
                    _tables text;
                BEGIN\s
                    SELECT string_agg(format('%I.%I', schemaname, tablename), ', ')\s
                    INTO _tables\s
                    FROM pg_tables\s
                    WHERE schemaname = 'public'; -- Altere o schema se necessário
                
                    IF _tables IS NOT NULL THEN\s
                        EXECUTE 'TRUNCATE TABLE ' || _tables || ' RESTART IDENTITY CASCADE';\s
                    END IF;\s
                END $$;
                """;

        jdbcTemplate.execute(sql);

    }

}

package com.db.migration.repository.target;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.db.migration.model.target.Department;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    //aqui ele diz ao intellij que iremos usar o banco novo nessa classe
    public DepartmentRepository(@Qualifier("newDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //metodo save q retorna o id da entidade salva
    public Long save(Department department) {

        //aqui fazemos a query q insere no banco as novas entidades
        String sql = """
                INSERT INTO departments (name, is_active, legacy_id, created_at)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                department.getName(),
                department.isActive(),
                department.getLegacyId(),
                Timestamp.from(department.getCreatedAt())
        );
    }


    public Long findIdByName(String name) {

        String sql = """
                SELECT id
                FROM departments
                WHERE lower(name) = lower(?)
                """;

        List<Long> ids = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                name
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

}

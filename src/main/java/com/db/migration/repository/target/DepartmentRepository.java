package com.db.migration.repository.target;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.db.migration.model.target.Department;

import javax.sql.DataSource;
import java.sql.Timestamp;

@Repository
public class DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepository (@Qualifier("newDataSource")DataSource newDataSource){
        this.jdbcTemplate = new JdbcTemplate(newDataSource);
    }

    public Long save(Department department){
        String sql = """
                INSERT INTO departments (name, is_Active, legacy_id, created_at)
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

}

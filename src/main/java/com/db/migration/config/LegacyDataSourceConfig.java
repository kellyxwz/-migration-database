package com.db.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class LegacyDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.legacy")
    public DataSource legacyDataSource(){
        return DataSourceBuilder.create().build();
    }
}

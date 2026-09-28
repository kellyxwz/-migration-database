package com.db.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class NewDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.newdb")
    public DataSource newDataSource(){
        return DataSourceBuilder.create().build();
    }

}

package com.taipower.intranet.persistence.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多資料庫來源
 *
 */
@Configuration
public class DynamicDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari.ds01")
    public DataSource dataSource01(){

        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari.ds02")
    public DataSource dataSource02(){

        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DynamicDataSource dataSource(DataSource dataSource01, DataSource dataSource02) {
        Map<Object, Object> targetDataSources = new HashMap<>(25);
        targetDataSources.put(DataSourceNames.DS01, dataSource01);
        targetDataSources.put(DataSourceNames.DS02, dataSource02);
        return new DynamicDataSource(dataSource01, targetDataSources);
    }

}

package com.norman.normanaiagent.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
    @ConfigurationProperties("spring.datasource.mysql")
    public DataSourceProperties mysqlProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
    public DataSource mysqlDataSource(@Qualifier("mysqlProperties") DataSourceProperties mysqlProperties) {
        return mysqlProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "spring.datasource.mysql", name = "url")
    public NamedParameterJdbcTemplate mysqlNamedParameterJdbcTemplate(@Qualifier("mysqlDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.postgres", name = "url")
    @ConfigurationProperties("spring.datasource.postgres")
    public DataSourceProperties postgresProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.postgres", name = "url")
    public DataSource postgresDataSource(@Qualifier("postgresProperties") DataSourceProperties postgresProperties) {
        return postgresProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.postgres", name = "url")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("postgresDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}

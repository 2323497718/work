package com.example.product_test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${app.datasource.write.url}")
    private String writeUrl;
    @Value("${app.datasource.write.username}")
    private String writeUsername;
    @Value("${app.datasource.write.password}")
    private String writePassword;
    @Value("${app.datasource.write.driver-class-name}")
    private String driverClassName;

    @Value("${app.datasource.read.url}")
    private String readUrl;
    @Value("${app.datasource.read.username}")
    private String readUsername;
    @Value("${app.datasource.read.password}")
    private String readPassword;

    @Bean(name = "writeDataSource")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(writeUrl)
                .username(writeUsername)
                .password(writePassword)
                .build();
    }

    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(readUrl)
                .username(readUsername)
                .password(readPassword)
                .build();
    }

    @Primary
    @Bean(name = "dataSource")
    public DataSource routingDataSource() {
        RoutingDataSource routing = new RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DbRole.WRITE, writeDataSource());
        targetDataSources.put(DbRole.READ, readDataSource());
        routing.setDefaultTargetDataSource(writeDataSource());
        routing.setTargetDataSources(targetDataSources);
        routing.afterPropertiesSet();
        return routing;
    }
}

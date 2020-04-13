package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;

import java.sql.Connection;

public class JdbcQueryFactory {

    private final ConfigurationFactory configurationFactory;
    private final Connection jdbcConnection;

    public JdbcQueryFactory(ConfigurationFactory configurationFactory, Connection jdbcConnection) {
        this.configurationFactory = configurationFactory;
        this.jdbcConnection = jdbcConnection;
    }

    public <T> SQLQuery<T> createQuery() {
        Configuration configuration = configurationFactory.createConfiguration();
        return new SQLQuery<>(jdbcConnection, configuration);
    }

}

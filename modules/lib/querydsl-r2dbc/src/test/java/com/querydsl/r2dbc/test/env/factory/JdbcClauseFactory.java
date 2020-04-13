package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.SQLInsertClause;

import java.sql.Connection;

public class JdbcClauseFactory {

    private final ConfigurationFactory configurationFactory;
    private final Connection connection;

    public JdbcClauseFactory(ConfigurationFactory configurationFactory, Connection connection) {
        this.configurationFactory = configurationFactory;
        this.connection = connection;
    }

    public SQLInsertClause insert(RelationalPath<?> entity) {
        Configuration configuration = configurationFactory.createConfiguration();
        return new SQLInsertClause(connection, configuration, entity);
    }

}

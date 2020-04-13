package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.dml.R2dbcDeleteClause;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

public class R2dbcClauseFactory {

    private final ConfigurationFactory configurationFactory;
    private final R2dbcConnectionProvider connectionProvider;

    public R2dbcClauseFactory(ConfigurationFactory configurationFactory, R2dbcConnectionProvider connectionProvider) {
        this.configurationFactory = configurationFactory;
        this.connectionProvider = connectionProvider;
    }

    public <T> R2dbcInsertClause createInsertClause(RelationalPath<?> entity) {
        Configuration configuration = configurationFactory.createConfiguration();
        return new R2dbcInsertClause(connectionProvider, configuration, entity);
    }

    public R2dbcUpdateClause createUpdateClause(RelationalPath<?> entity) {
        Configuration configuration = configurationFactory.createConfiguration();
        return new R2dbcUpdateClause(connectionProvider, configuration, entity);
    }

    public R2dbcDeleteClause createDeleteClause(RelationalPath<?> entity) {
        Configuration configuration = configurationFactory.createConfiguration();
        return new R2dbcDeleteClause(connectionProvider, configuration, entity);
    }

}

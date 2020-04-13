package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.R2dbcQuery;
import com.querydsl.sql.Configuration;

public class R2dbcQueryFactory {

    private final ConfigurationFactory configurationFactory;
    private final R2dbcConnectionProvider connectionProvider;

    public R2dbcQueryFactory(ConfigurationFactory configurationFactory, R2dbcConnectionProvider connectionProvider) {
        this.configurationFactory = configurationFactory;
        this.connectionProvider = connectionProvider;
    }

    public <T> R2dbcQuery<T> createQuery() {
        Configuration configuration = configurationFactory.createConfiguration();
        return new R2dbcQuery<>(connectionProvider, configuration);
    }

}

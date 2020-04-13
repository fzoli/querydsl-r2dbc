package com.querydsl.r2dbc.postgresql;


import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.R2dbcQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 * {@link PostgreSqlR2dbcQuery} provides PostgreSQL related extensions to {@link R2dbcQuery}.
 *
 * If you need to subtype this, use the base class instead.
 *
 * @param <T> the result type
 */
public class PostgreSqlR2dbcQuery<T> extends AbstractPostgreSqlR2dbcQuery<T, PostgreSqlR2dbcQuery<T>> {

    public PostgreSqlR2dbcQuery(R2dbcConnectionProvider connProvider) {
        this(connProvider, new Configuration(PostgreSQLTemplates.DEFAULT), new DefaultQueryMetadata());
    }

    public PostgreSqlR2dbcQuery(R2dbcConnectionProvider connProvider, SQLTemplates templates) {
        this(connProvider, new Configuration(templates), new DefaultQueryMetadata());
    }

    public PostgreSqlR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration, QueryMetadata metadata) {
        super(connProvider, configuration, metadata);
    }

    public PostgreSqlR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration) {
        super(connProvider, configuration, new DefaultQueryMetadata());
    }

    @Override
    public PostgreSqlR2dbcQuery<T> clone(R2dbcConnectionProvider connectionProvider) {
        PostgreSqlR2dbcQuery<T> q = new PostgreSqlR2dbcQuery<T>(connectionProvider, getConfiguration(), getMetadata().clone());
        q.clone(this);
        return q;
    }

    @Override
    public <U> PostgreSqlR2dbcQuery<U> select(Expression<U> expr) {
        queryMixin.setProjection(expr);
        @SuppressWarnings("unchecked") // This is the new type
        PostgreSqlR2dbcQuery<U> newType = (PostgreSqlR2dbcQuery<U>) this;
        return newType;
    }

    @Override
    public PostgreSqlR2dbcQuery<Tuple> select(Expression<?>... exprs) {
        queryMixin.setProjection(exprs);
        @SuppressWarnings("unchecked") // This is the new type
        PostgreSqlR2dbcQuery<Tuple> newType = (PostgreSqlR2dbcQuery<Tuple>) this;
        return newType;
    }

}

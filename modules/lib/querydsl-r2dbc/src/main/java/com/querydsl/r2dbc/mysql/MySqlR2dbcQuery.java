package com.querydsl.r2dbc.mysql;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.R2dbcQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;

/**
 * {@link MySqlR2dbcQuery} provides MySQL related extensions to {@link R2dbcQuery}.
 *
 * If you need to subtype this, use the base class instead.
 *
 * @param <T> the result type
 */
public class MySqlR2dbcQuery<T> extends AbstractMySqlR2dbcQuery<T, MySqlR2dbcQuery<T>> {

    public MySqlR2dbcQuery(R2dbcConnectionProvider connProvider) {
        this(connProvider, new Configuration(MySQLTemplates.DEFAULT), new DefaultQueryMetadata());
    }

    public MySqlR2dbcQuery(R2dbcConnectionProvider connProvider, SQLTemplates templates) {
        this(connProvider, new Configuration(templates), new DefaultQueryMetadata());
    }

    public MySqlR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration, QueryMetadata metadata) {
        super(connProvider, configuration, metadata);
    }

    public MySqlR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration) {
        super(connProvider, configuration, new DefaultQueryMetadata());
    }

    @Override
    public MySqlR2dbcQuery<T> clone(R2dbcConnectionProvider connectionProvider) {
        MySqlR2dbcQuery<T> q = new MySqlR2dbcQuery<T>(connectionProvider, getConfiguration(), getMetadata().clone());
        q.clone(this);
        return q;
    }

    @Override
    public <U> MySqlR2dbcQuery<U> select(Expression<U> expr) {
        queryMixin.setProjection(expr);

        @SuppressWarnings("unchecked") // This is the new type
        MySqlR2dbcQuery<U> res = (MySqlR2dbcQuery<U>) this;
        return res;
    }

    @Override
    public MySqlR2dbcQuery<Tuple> select(Expression<?>... exprs) {
        queryMixin.setProjection(exprs);

        @SuppressWarnings("unchecked") // This is the new type
        MySqlR2dbcQuery<Tuple> res = (MySqlR2dbcQuery<Tuple>) this;
        return res;
    }

}

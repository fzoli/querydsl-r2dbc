package com.querydsl.r2dbc;


import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLCommonQuery;

/**
 * {@link R2dbcQuery} is a R2DBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @param <T>
 */
public class R2dbcQuery<T> extends AbstractR2dbcQuery<T, R2dbcQuery<T>> {

    /**
     * Create a new SQLQuery instance
     *
     * @param connProvider Connection to use
     * @param configuration configuration
     */
    public R2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration) {
        super(connProvider, configuration);
    }

    /**
     * Create a new SQLQuery instance
     *
     * @param connProvider Connection to use
     * @param configuration configuration
     * @param metadata metadata
     */
    public R2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration, QueryMetadata metadata) {
        super(connProvider, configuration, metadata);
    }


    @Override
    public R2dbcQuery<T> clone(R2dbcConnectionProvider connectionProvider) {
        R2dbcQuery<T> q = new R2dbcQuery<T>(connectionProvider, getConfiguration(), getMetadata().clone());
        q.clone(this);
        return q;
    }

    @Override
    public <U> R2dbcQuery<U> select(Expression<U> expr) {
        queryMixin.setProjection(expr);
        @SuppressWarnings("unchecked") // This is the new type
        R2dbcQuery<U> newType = (R2dbcQuery<U>) this;
        return newType;
    }

    @Override
    public R2dbcQuery<Tuple> select(Expression<?>... exprs) {
        queryMixin.setProjection(exprs);
        @SuppressWarnings("unchecked") // This is the new type
        R2dbcQuery<Tuple> newType = (R2dbcQuery<Tuple>) this;
        return newType;
    }

}

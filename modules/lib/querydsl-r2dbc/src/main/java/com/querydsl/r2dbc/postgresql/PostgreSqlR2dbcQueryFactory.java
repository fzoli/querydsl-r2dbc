package com.querydsl.r2dbc.postgresql;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.AbstractR2dbcQueryFactory;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.*;

/**
 * PostgreSQL specific implementation
 */
public class PostgreSqlR2dbcQueryFactory extends AbstractR2dbcQueryFactory<PostgreSqlR2dbcQuery<?>> {

    public PostgreSqlR2dbcQueryFactory(R2dbcConnectionProvider connProvider) {
        this(connProvider, new Configuration(new PostgreSQLTemplates()));
    }

    public PostgreSqlR2dbcQueryFactory(R2dbcConnectionProvider connProvider, SQLTemplates templates) {
        this(connProvider, new Configuration(templates));
    }

    public PostgreSqlR2dbcQueryFactory(R2dbcConnectionProvider connProvider, Configuration configuration) {
        super(connProvider, configuration);
    }

    @Override
    public PostgreSqlR2dbcQuery<?> query() {
        return new PostgreSqlR2dbcQuery<Void>(connection, configuration);
    }

    @Override
    public <T> PostgreSqlR2dbcQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public PostgreSqlR2dbcQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> PostgreSqlR2dbcQuery<T> selectDistinct(Expression<T> expr) {
        return query().select(expr).distinct();
    }

    @Override
    public PostgreSqlR2dbcQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

    @Override
    public PostgreSqlR2dbcQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public PostgreSqlR2dbcQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public <T> PostgreSqlR2dbcQuery<T> selectFrom(RelationalPath<T> expr) {
        return select(expr).from(expr);
    }

}

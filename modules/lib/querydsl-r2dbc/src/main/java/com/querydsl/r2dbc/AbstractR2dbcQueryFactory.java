package com.querydsl.r2dbc;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.r2dbc.dml.R2dbcDeleteClause;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLCommonQuery;

public abstract class AbstractR2dbcQueryFactory<Q extends SQLCommonQuery<?>> implements QueryFactory<Q> {

    protected final Configuration configuration;

    protected final R2dbcConnectionProvider connection;

    public AbstractR2dbcQueryFactory(R2dbcConnectionProvider connProvider, Configuration configuration) {
        this.configuration = configuration;
        this.connection = connProvider;
    }

    public final Configuration getConfiguration() {
        return configuration;
    }

    public final R2dbcInsertClause insert(RelationalPath<?> path) {
        return new R2dbcInsertClause(connection, configuration, path);
    }

    public final R2dbcUpdateClause update(RelationalPath<?> path) {
        return new R2dbcUpdateClause(connection, configuration, path);
    }

    public final R2dbcDeleteClause delete(RelationalPath<?> path) {
        return new R2dbcDeleteClause(connection, configuration, path);
    }

    @SuppressWarnings("unchecked")
    public final Q from(Expression<?> from) {
        return (Q) query().from(from);
    }

    @SuppressWarnings("unchecked")
    public final Q from(Expression<?>... args) {
        return (Q) query().from(args);
    }

    @SuppressWarnings("unchecked")
    public final Q from(SubQueryExpression<?> subQuery, Path<?> alias) {
        return (Q) query().from(subQuery, alias);
    }

    /**
     * Create a new SQL query with the given projection
     *
     * @param expr projection
     * @param <T> type of the projection
     * @return select(expr)
     */
    public abstract <T> AbstractR2dbcQuery<T, ?> select(Expression<T> expr);

    /**
     * Create a new SQL query with the given projection
     *
     * @param exprs projection
     * @return select(exprs)
     */
    public abstract AbstractR2dbcQuery<Tuple, ?> select(Expression<?>... exprs);

    /**
     * Create a new SQL query with the given projection
     *
     * @param expr distinct projection
     * @param <T> type of the projection
     * @return select(distinct expr)
     */
    public abstract <T> AbstractR2dbcQuery<T, ?> selectDistinct(Expression<T> expr);

    /**
     * Create a new SQL query with the given projection
     *
     * @param exprs distinct projection
     * @return select(distinct exprs)
     */
    public abstract AbstractR2dbcQuery<Tuple, ?> selectDistinct(Expression<?>... exprs);

    /**
     * Create a new SQL query with zero as the projection
     *
     * @return select(0)
     */
    public abstract AbstractR2dbcQuery<Integer, ?> selectZero();

    /**
     * Create a new SQL query with one as the projection
     *
     * @return select(1)
     */
    public abstract AbstractR2dbcQuery<Integer, ?> selectOne();

    /**
     * Create a new SQL query with the given projection and source
     *
     * @param expr query source and projection
     * @param <T> type of the projection
     * @return select(expr).from(expr)
     */
    public abstract <T> AbstractR2dbcQuery<T, ?> selectFrom(RelationalPath<T> expr);

}

package com.querydsl.r2dbc.mysql;

import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.AbstractR2dbcQueryFactory;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLTemplates;

/**
 * MySQL specific implementation
 */
public class MySqlR2dbcQueryFactory extends AbstractR2dbcQueryFactory<MySqlR2dbcQuery<?>> {

    public MySqlR2dbcQueryFactory(R2dbcConnectionProvider connection) {
        this(connection, new Configuration(new MySQLTemplates()));
    }

    public MySqlR2dbcQueryFactory(R2dbcConnectionProvider connection, SQLTemplates templates) {
        this(connection, new Configuration(templates));
    }

    public MySqlR2dbcQueryFactory(R2dbcConnectionProvider connection, Configuration configuration) {
        super(connection, configuration);
    }

    /**
     * Create a INSERT IGNORE INTO clause
     *
     * @param entity table to insert to
     * @return insert clause
     */
    public R2dbcInsertClause insertIgnore(RelationalPath<?> entity) {
        R2dbcInsertClause insert = insert(entity);
        insert.addFlag(Position.START_OVERRIDE, "insert ignore into ");
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clause clause
     * @return insert clause
     */
    public R2dbcInsertClause insertOnDuplicateKeyUpdate(RelationalPath<?> entity, String clause) {
        R2dbcInsertClause insert = insert(entity);
        insert.addFlag(Position.END, " on duplicate key update " + clause);
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clause clause
     * @return insert clause
     */
    public R2dbcInsertClause insertOnDuplicateKeyUpdate(RelationalPath<?> entity, Expression<?> clause) {
        R2dbcInsertClause insert = insert(entity);
        insert.addFlag(Position.END, ExpressionUtils.template(String.class, " on duplicate key update {0}", clause));
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clauses clauses
     * @return insert clause
     */
    public R2dbcInsertClause insertOnDuplicateKeyUpdate(RelationalPath<?> entity, Expression<?>... clauses) {
        R2dbcInsertClause insert = insert(entity);
        StringBuilder flag = new StringBuilder(" on duplicate key update ");
        for (int i = 0; i < clauses.length; i++) {
            flag.append(i > 0 ? ", " : "").append("{" + i + "}");
        }
        Object[] clauseArray = clauses;
        insert.addFlag(Position.END, ExpressionUtils.template(String.class, flag.toString(), clauseArray));
        return insert;
    }

    @Override
    public MySqlR2dbcQuery<?> query() {
        return new MySqlR2dbcQuery<Void>(connection, configuration);
    }

    public MySqlR2dbcReplaceClause replace(RelationalPath<?> entity) {
        return new MySqlR2dbcReplaceClause(connection, configuration, entity);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public MySqlR2dbcQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> selectDistinct(Expression<T> expr) {
        return query().select(expr).distinct();
    }

    @Override
    public MySqlR2dbcQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

    @Override
    public MySqlR2dbcQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public MySqlR2dbcQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public <T> MySqlR2dbcQuery<T> selectFrom(RelationalPath<T> expr) {
        return select(expr).from(expr);
    }

}

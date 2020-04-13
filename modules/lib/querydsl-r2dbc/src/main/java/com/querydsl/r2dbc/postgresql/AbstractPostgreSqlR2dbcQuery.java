package com.querydsl.r2dbc.postgresql;

import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.r2dbc.AbstractR2dbcQuery;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.R2dbcQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

/**
 * {@link PostgreSqlR2dbcQuery} provides PostgreSQL related extensions to {@link R2dbcQuery}
 *
 * @param <T> result type
 * @param <C> the concrete subtype
 *
 * @see R2dbcQuery
 */
public abstract class AbstractPostgreSqlR2dbcQuery<T, C extends AbstractPostgreSqlR2dbcQuery<T, C>> extends AbstractR2dbcQuery<T, C> {

    public AbstractPostgreSqlR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration, QueryMetadata metadata) {
        super(connProvider, configuration, metadata);
    }

    /**
     * FOR SHARE causes the rows retrieved by the SELECT statement to be locked as though for update.
     *
     * @return the current object
     */
    public C forShare() {
        // global forShare support was added later, delegating to super implementation
        return super.forShare();
    }

    /**
     * With NOWAIT, the statement reports an error, rather than waiting, if a selected row cannot
     * be locked immediately.
     *
     * @return the current object
     */
    public C noWait() {
        QueryFlag noWaitFlag = configuration.getTemplates().getNoWaitFlag();
        return addFlag(noWaitFlag);
    }

    /**
     * FOR UPDATE / FOR SHARE OF tables
     *
     * @param paths tables
     * @return the current object
     */
    public C of(RelationalPath<?>... paths) {
        StringBuilder builder = new StringBuilder(" of ");
        for (RelationalPath<?> path : paths) {
            if (builder.length() > 4) {
                builder.append(", ");
            }
            builder.append(getConfiguration().getTemplates().quoteIdentifier(path.getTableName()));
        }
        return addFlag(Position.END, builder.toString());
    }

    /**
     * adds a DISTINCT ON clause
     *
     * @param exprs
     * @return
     */
    public C distinctOn(Expression<?>... exprs) {
        return addFlag(Position.AFTER_SELECT,
                Expressions.template(Object.class, "distinct on({0}) ",
                        ExpressionUtils.list(Object.class, exprs)));
    }

}

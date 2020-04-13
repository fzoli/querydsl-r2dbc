package com.querydsl.r2dbc.dml;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

/**
 * Defines an UPDATE clause.
 * If you need to subtype this, use {@link AbstractR2dbcUpdateClause} instead.
 */
public class R2dbcUpdateClause extends AbstractR2dbcUpdateClause<R2dbcUpdateClause> {

    public R2dbcUpdateClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

}

package com.querydsl.r2dbc.dml;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

/**
 * {@link R2dbcDeleteClause} defines a DELETE clause.
 * If you need to subtype this, use {@link AbstractR2dbcDeleteClause} instead.
 */
public class R2dbcDeleteClause extends AbstractR2dbcDeleteClause<R2dbcDeleteClause> {

    public R2dbcDeleteClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

}

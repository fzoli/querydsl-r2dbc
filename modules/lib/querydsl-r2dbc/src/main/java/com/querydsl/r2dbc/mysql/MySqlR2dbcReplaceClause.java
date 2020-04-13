package com.querydsl.r2dbc.mysql;

import com.querydsl.core.QueryFlag;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;

/**
 * {@link MySqlR2dbcReplaceClause} is a REPLACE INTO clause
 *
 * <p>REPLACE works exactly like INSERT, except that if an old row in the table has the same value
 * as a new row for a PRIMARY KEY or a UNIQUE index, the old row is deleted before the new row is inserted.</p>
 *
 */
public class MySqlR2dbcReplaceClause extends R2dbcInsertClause {

    protected static final String REPLACE_INTO = "replace into ";

    public MySqlR2dbcReplaceClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
        addFlag(QueryFlag.Position.START_OVERRIDE, REPLACE_INTO);
    }

}

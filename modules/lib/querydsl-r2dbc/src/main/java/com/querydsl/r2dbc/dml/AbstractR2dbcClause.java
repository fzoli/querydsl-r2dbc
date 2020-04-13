package com.querydsl.r2dbc.dml;

import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.corereactive.dml.DMLClause;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * {@link AbstractR2dbcClause} is a superclass for SQL based DMLClause implementations
 *
 * @param <C> concrete subtype
 */
public abstract class AbstractR2dbcClause<C extends AbstractR2dbcClause<C>> implements DMLClause<C> {

    protected final Configuration configuration;
    private final R2dbcConnectionProvider connProvider;

    protected boolean useLiterals;

    public AbstractR2dbcClause(R2dbcConnectionProvider connProvider, Configuration configuration) {
        this.connProvider = connProvider;
        this.configuration = configuration;
        this.useLiterals = configuration.getUseLiterals();
    }

    @SuppressWarnings("unchecked")
    protected final C self() {
        return (C) this;
    }

    /**
     * Set whether literals are used in SQL strings instead of parameter bindings (default: false)
     *
     * <p>Warning: When literals are used, prepared statement won't have any parameter bindings
     * and batch statements are not supported.</p>
     *
     * @param useLiterals true for literals and false for bindings
     */
    public final void setUseLiterals(boolean useLiterals) {
        this.useLiterals = useLiterals;
    }

    public final C withUseLiterals() {
        setUseLiterals(true);
        return self();
    }

    protected final Mono<Connection> requireConnection() {
        if (connProvider != null) {
            return connProvider.getConnection();
        } else {
            throw new IllegalStateException("No connection provided");
        }
    }

    protected final void setParameters(
            Statement stmt, List<?> objects,
            List<Path<?>> constantPaths, Map<ParamExpression<?>, ?> params, int offset) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() + " paths, " +
                    "but got " + constantPaths.size());
        }
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (o instanceof ParamExpression) {
                if (!params.containsKey(o)) {
                    throw new ParamNotSetException((ParamExpression<?>) o);
                }
                o = params.get(o);
            }
            bind(stmt, constantPaths.get(i), (offset * objects.size()) + i, o);
        }
    }

    private <T> void bind(Statement stmt, Path<?> path, int i, T value) {
        if (value == null || value instanceof Null) {
            if (path != null) {
                stmt.bindNull(i, path.getType());
            }
        } else {
            stmt.bind(i, value);
        }
    }

}

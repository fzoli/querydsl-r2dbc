package com.querydsl.r2dbc;

import com.google.common.collect.Lists;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryException;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.corereactive.types.dsl.OptionalExpression;
import com.querydsl.r2dbc.internal.R2dbcUtils;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import io.r2dbc.spi.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link AbstractR2dbcQuery} is the base type for SQL query implementations
 *
 * @param <T> result type
 * @param <Q> concrete subtype
 *
 */
public abstract class AbstractR2dbcQuery<T, Q extends AbstractR2dbcQuery<T, Q>> extends ProjectableR2dbcQuery<T, Q> {

    private final R2dbcConnectionProvider connProvider;

    protected boolean useLiterals;

    public AbstractR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration) {
        this(connProvider, configuration, new DefaultQueryMetadata());
    }

    public AbstractR2dbcQuery(R2dbcConnectionProvider connProvider, Configuration configuration, QueryMetadata metadata) {
        super(new QueryMixin<Q>(metadata, false), configuration);
        this.connProvider = connProvider;
        this.useLiterals = configuration.getUseLiterals();
    }

    /**
     * Create an alias for the expression
     *
     * @param alias alias
     * @return this as alias
     */
    public SimpleExpression<T> as(String alias) {
        return Expressions.as(this, alias);
    }

    /**
     * Create an alias for the expression
     *
     * @param alias alias
     * @return this as alias
     */
    @SuppressWarnings("unchecked")
    public SimpleExpression<T> as(Path<?> alias) {
        return Expressions.as(this, (Path) alias);
    }

    /**
     * If you use forUpdate() with a backend that uses page or row locks, rows examined by the
     * query are write-locked until the end of the current transaction.
     *
     * Not supported for SQLite and CUBRID
     *
     * @return the current object
     */
    public Q forUpdate() {
        QueryFlag forUpdateFlag = configuration.getTemplates().getForUpdateFlag();
        return addFlag(forUpdateFlag);
    }

    /**
     * FOR SHARE causes the rows retrieved by the SELECT statement to be locked as though for update.
     *
     * Supported by MySQL, PostgreSQL, SQLServer.
     *
     * @return the current object
     *
     * @throws QueryException
     *          if the FOR SHARE is not supported.
     */
    public Q forShare() {
        return forShare(false);
    }

    /**
     * FOR SHARE causes the rows retrieved by the SELECT statement to be locked as though for update.
     *
     * Supported by MySQL, PostgreSQL, SQLServer.
     *
     * @param fallbackToForUpdate
     *          if the FOR SHARE is not supported and this parameter is <code>true</code>, the
     *          {@link #forUpdate()} functionality will be used.
     *
     * @return the current object
     *
     * @throws QueryException
     *          if the FOR SHARE is not supported and <i>fallbackToForUpdate</i> is set to
     *          <code>false</code>.
     */
    public Q forShare(boolean fallbackToForUpdate) {
        SQLTemplates sqlTemplates = configuration.getTemplates();

        if (sqlTemplates.isForShareSupported()) {
            QueryFlag forShareFlag = sqlTemplates.getForShareFlag();
            return addFlag(forShareFlag);
        }

        if (fallbackToForUpdate) {
            return forUpdate();
        }

        throw new QueryException("Using forShare() is not supported");
    }

    @Override
    protected SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    /**
     * Set whether literals are used in SQL strings instead of parameter bindings (default: false)
     *
     * <p>Warning: When literals are used, prepared statement won't have any parameter bindings
     * and batch statements are not supported.</p>
     *
     * @param useLiterals true for literals and false for bindings
     */
    public void setUseLiterals(boolean useLiterals) {
        this.useLiterals = useLiterals;
    }

    @SuppressWarnings("unchecked")
    public Q withUseLiterals() {
        setUseLiterals(true);
        return (Q) this;
    }

    @Override
    protected void clone(Q query) {
        super.clone(query);
        this.useLiterals = query.useLiterals;
    }

    @Override
    public Q clone() {
        return this.clone(this.connProvider);
    }

    public abstract Q clone(R2dbcConnectionProvider connectionProvider);

    @Override
    public Flux<T> fetch() {
        return requireConnection().flatMapMany(conn -> {
            Expression<T> expr = getProjection();
            Mapper<T> mapper = createMapper(expr);
            SQLSerializer serializer = serialize(false);
            String originalSql = serializer.toString();
            String sql = R2dbcUtils.replaceBindingArguments(originalSql);
            Statement statement = bind(conn.createStatement(sql), serializer);
            return Flux.from(statement.execute()).flatMap(result -> result.map(mapper::map));
        });
    }

    private Mono<Connection> requireConnection() {
        if (connProvider != null) {
            return connProvider.getConnection();
        } else {
            throw new IllegalStateException("No connection provided");
        }
    }

    @SuppressWarnings("unchecked")
    private Expression<T> getProjection() {
        return (Expression<T>) queryMixin.getMetadata().getProjection();
    }

    private Mapper<T> createMapper(Expression<T> expr) {
        if (expr instanceof FactoryExpression) {
            FactoryExpression<T> fe = (FactoryExpression<T>) expr;
            return (row, meta) -> newInstance(fe, row, 0);
        } else if (expr.equals(Wildcard.all)) {
            return this::toWildcardObjectArray;
        } else {
            if (expr instanceof OptionalExpression) {
                OptionalExpression<T> oe = (OptionalExpression<T>) expr;
                return (row, meta) -> asOptional(row, oe);
            }
            return (row, meta) -> asRequired(row, expr);
        }
    }

    private T asRequired(Row row, Expression<T> expr) {
        return Objects.requireNonNull(row.get(0, expr.getType()), "Null result");
    }

    @SuppressWarnings("unchecked")
    private T asOptional(Row row, OptionalExpression<T> oe) {
        return (T) Optional.ofNullable(row.get(0, oe.getWrappedType()));
    }

    @Nonnull
    private T toWildcardObjectArray(Row row, RowMetadata meta) {
        ArrayList<? extends ColumnMetadata> metaList = Lists.newArrayList(meta.getColumnMetadatas());
        Object[] args = new Object[metaList.size()];
        for (int i = 0; i < args.length; i++) {
            ColumnMetadata columnMetadata = metaList.get(i);
            args[i] = row.get(i, Objects.requireNonNull(columnMetadata.getJavaType(), "Unknown Java type"));
        }
        @SuppressWarnings("unchecked")
        T result = (T) args;
        return result;
    }

    @Nonnull
    private T newInstance(FactoryExpression<T> c, Row rs, int offset) {
        Object[] args = new Object[c.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = get(rs, c.getArgs().get(i), offset + i);
        }
        return Objects.requireNonNull(c.newInstance(args), "Null result");
    }

    private Object get(Row rs, Expression<?> expr, int i) {
        return rs.get(i, expr.getType());
    }

    private Statement bind(Statement statement, SQLSerializer serializer) {
        List<Object> args = serializer.getConstants();
        for (int i = 0; i < args.size(); i++) {
            statement.bind(i, args.get(i));
        }
        return statement;
    }

    @FunctionalInterface
    private interface Mapper<T> {
        @Nonnull
        T map(Row row, RowMetadata metadata);
    }

}

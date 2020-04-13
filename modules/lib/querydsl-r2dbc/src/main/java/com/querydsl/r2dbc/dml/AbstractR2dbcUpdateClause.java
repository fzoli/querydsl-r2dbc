package com.querydsl.r2dbc.dml;

import com.google.common.collect.Maps;
import com.querydsl.core.*;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.types.*;
import com.querydsl.corereactive.dml.UpdateClause;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.internal.R2dbcUtils;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLUpdateBatch;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a base class for dialect-specific UPDATE clauses.
 *
 * @param <C> The type extending this class.
 */
public abstract class AbstractR2dbcUpdateClause<C extends AbstractR2dbcUpdateClause<C>> extends AbstractR2dbcClause<C> implements UpdateClause<C> {

    protected final RelationalPath<?> entity;

    protected final List<SQLUpdateBatch> batches = new ArrayList<>();

    protected Map<Path<?>, Expression<?>> updates = Maps.newLinkedHashMap();

    protected QueryMetadata metadata = new DefaultQueryMetadata();

    public AbstractR2dbcUpdateClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration);
        this.entity = entity;
        metadata.addJoin(JoinType.DEFAULT, entity);
    }

    @Override
    public String toString() {
        SQLSerializer serializer = createSerializerAndSerialize();
        return serializer.toString();
    }

    /**
     * Add the given String literal at the given position as a query flag
     *
     * @param position position
     * @param flag query flag
     * @return the current object
     */
    public C addFlag(Position position, String flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    /**
     * Add the given Expression at the given position as a query flag
     *
     * @param position position
     * @param flag query flag
     * @return the current object
     */
    public C addFlag(Position position, Expression<?> flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    /**
     * Add the current state of bindings as a batch item.
     *
     * Note that each state must contain the same column list with the same order.
     *
     * @return the current object
     */
    public C addBatch() {
        batches.add(new SQLUpdateBatch(metadata, updates));
        updates = Maps.newLinkedHashMap();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        return self();
    }

    /**
     * Clear the internal state of the clause
     */
    public void clear() {
        batches.clear();
        updates = Maps.newLinkedHashMap();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
    }

    @Override
    public <T> C set(Path<T> path, T value) {
        if (value instanceof Expression<?>) {
            updates.put(path, (Expression<?>) value);
        } else if (value != null) {
            updates.put(path, ConstantImpl.create(value));
        } else {
            setNull(path);
        }
        return self();
    }

    @Override
    public <T> C set(Path<T> path, Expression<? extends T> expression) {
        if (expression != null) {
            updates.put(path, expression);
        } else {
            setNull(path);
        }
        return self();
    }

    @Override
    public <T> C setNull(Path<T> path) {
        updates.put(path, Null.CONSTANT);
        return self();
    }

    @Override
    public C set(List<? extends Path<?>> paths, List<?> values) {
        for (int i = 0; i < paths.size(); i++) {
            if (values.get(i) instanceof Expression) {
                updates.put(paths.get(i), (Expression<?>) values.get(i));
            } else if (values.get(i) != null) {
                updates.put(paths.get(i), ConstantImpl.create(values.get(i)));
            } else {
                updates.put(paths.get(i), Null.CONSTANT);
            }
        }
        return self();
    }

    public C where(Predicate p) {
        metadata.addWhere(p);
        return self();
    }

    @Override
    public C where(Predicate... o) {
        for (Predicate p : o) {
            metadata.addWhere(p);
        }
        return self();
    }

    public C limit(@Nonnegative long limit) {
        metadata.setModifiers(QueryModifiers.limit(limit));
        return self();
    }

    @Override
    public boolean isEmpty() {
        return updates.isEmpty() && batches.isEmpty();
    }

    public int getBatchCount() {
        return batches.size();
    }

    @Override
    public Mono<Long> execute() {
        if (batches.isEmpty()) {
            return requireConnection()
                    .map(this::createStatement)
                    .flatMap(this::executeStatement);
        } else {
            return requireConnection()
                    .map(this::createStatement)
                    .flatMapMany(this::executeNonBulkBatchStatement)
                    .reduce(0L, Long::sum);
        }
    }

    private Mono<Long> executeStatement(Statement stmt) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Flux<Long> executeNonBulkBatchStatement(Statement stmt) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private Statement createStatement(Connection connection) {
        SQLSerializer serializer = createSerializerAndSerialize();
        return prepareStatementAndSetParameters(connection, serializer);
    }

    private Statement prepareStatementAndSetParameters(Connection connection, SQLSerializer serializer) {
        String queryString = serializer.toString();
        queryString = R2dbcUtils.replaceBindingArguments(queryString);
        Statement stmt = connection.createStatement(queryString);
        if (batches.isEmpty()) {
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams(), 0);
        } else {
            int offset = 0;
            for (SQLUpdateBatch batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch updates are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                stmt.add();
            }
        }
        return stmt;
    }

    private <T> void setBatchParameters(Statement stmt, SQLUpdateBatch batch, int offset) {
        SQLSerializer helperSerializer = createSerializer();
        helperSerializer.serializeUpdate(batch.getMetadata(), entity, batch.getUpdates());
        setParameters(stmt, helperSerializer.getConstants(), helperSerializer.getConstantPaths(), batch.getMetadata().getParams(), offset);
    }

    private SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty()) {
            SQLUpdateBatch first = batches.get(0);
            serializer.serializeUpdate(first.getMetadata(), entity, first.getUpdates());
        } else {
            serializer.serializeUpdate(metadata, entity, updates);
        }
        return serializer;
    }

}

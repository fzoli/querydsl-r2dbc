package com.querydsl.r2dbc.dml;

import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.JoinType;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.*;
import com.querydsl.corereactive.dml.InsertClause;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.internal.R2dbcUtils;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.types.Null;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides a base class for dialect-specific INSERT clauses.
 *
 * @param <C> The type extending this class.
 */
public abstract class AbstractR2dbcInsertClause<C extends AbstractR2dbcInsertClause<C>> extends AbstractR2dbcClause<C> implements InsertClause<C> {

    private final List<SQLInsertBatch> batches = new ArrayList<SQLInsertBatch>();

    private final List<Path<?>> columns = new ArrayList<Path<?>>();

    private final List<Expression<?>> values = new ArrayList<Expression<?>>();

    private final RelationalPath<?> entity;

    private final QueryMetadata metadata = new DefaultQueryMetadata();

    @Nullable
    private SubQueryExpression<?> subQuery;

    private transient boolean batchToBulk;

    public AbstractR2dbcInsertClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
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
    public C addFlag(QueryFlag.Position position, String flag) {
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
    public C addFlag(QueryFlag.Position position, Expression<?> flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return self();
    }

    /**
     * Set batches to be optimized into a single bulk operation.
     * Will revert to batches, if bulk is not supported
     * @return this
     */
    public C withBatchToBulk() {
        setBatchToBulk(true);
        return self();
    }

    /**
     * Set whether batches should be optimized into a single bulk operation.
     * Will revert to batches, if bulk is not supported
     */
    public void setBatchToBulk(boolean b) {
        this.batchToBulk = b && configuration.getTemplates().isBatchToBulkSupported();
    }

    private SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty() && batchToBulk) {
            serializer.serializeInsert(metadata, entity, batches);
        } else if (!batches.isEmpty()) {
            SQLInsertBatch first = batches.get(0);
            serializer.serializeInsert(metadata, entity, first.getColumns(), first.getValues(), subQuery);
        } else {
            serializer.serializeInsert(metadata, entity, columns, values, subQuery);
        }
        return serializer;
    }

    /**
     * Clear the internal state of the clause
     */
    public void clear() {
        batches.clear();
        columns.clear();
        values.clear();
        subQuery = null;
    }

    @Override
    public C columns(Path<?>... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return self();
    }

    @Override
    public C select(SubQueryExpression<?> sq) {
        this.subQuery = sq;
        for (Map.Entry<ParamExpression<?>, Object> entry : sq.getMetadata().getParams().entrySet()) {
            @SuppressWarnings("unchecked")
            ParamExpression<Object> key = (ParamExpression<Object>) entry.getKey();
            metadata.setParam(key, entry.getValue());
        }
        return self();
    }

    @Override
    public C values(Object... v) {
        for (Object value : v) {
            if (value instanceof Expression<?>) {
                values.add((Expression<?>) value);
            } else if (value != null) {
                values.add(ConstantImpl.create(value));
            } else {
                values.add(Null.CONSTANT);
            }
        }
        return self();
    }

    @Override
    public <T> C set(Path<T> path, @Nullable T value) {
        columns.add(path);
        if (value instanceof Expression<?>) {
            values.add((Expression<?>) value);
        } else if (value != null) {
            values.add(ConstantImpl.create(value));
        } else {
            values.add(Null.CONSTANT);
        }
        return self();
    }

    @Override
    public <T> C set(Path<T> path, Expression<? extends T> expression) {
        columns.add(path);
        values.add(expression);
        return self();
    }

    @Override
    public <T> C setNull(Path<T> path) {
        columns.add(path);
        values.add(Null.CONSTANT);
        return self();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty() && batches.isEmpty();
    }

    /**
     * Add the current state of bindings as a batch item.
     *
     * Note that each state must contain the same column list with the same order.
     *
     * @return the current object
     */
    public C addBatch() {
        batches.add(new SQLInsertBatch(columns, values, subQuery));
        columns.clear();
        values.clear();
        subQuery = null;
        return self();
    }

    /**
     * Execute the clause and return the generated key with the type of the
     * given path. If no rows were created, or the referenced column is not a
     * generated primary key, empty is returned, otherwise the primary key
     * of the inserted row is returned.
     *
     * @param path path for key
     * @return generated key (the first in case of batch insert)
     */
    @SuppressWarnings("unchecked")
    public <T> Mono<T> executeWithKey(Path<T> path) {
        return executeWithKey((Class<T>) path.getType(), path);
    }

    private <T> Mono<T> executeWithKey(Class<T> type, @Nullable Path<T> path) {
        Mapper<T> mapper = (row, metadata) -> Objects.requireNonNull(row.get(0, type), "Null key result");
        return requireConnection()
                .map(connection -> createStatement(connection, true))
                .flatMap(connection -> executeStatementWithKey(connection, mapper));
    }

    /**
     * Execute the clause and return the generated key with the type of the
     * given path. If no rows were created, or the referenced column is not a
     * generated primary key, empty is returned. Otherwise, the generated keys of the rows are
     * returned.
     *
     * <p>Note that {@link com.querydsl.sql.dml.SQLInsertClause#executeWithKeys(Path)} has different behaviour:
     * That returns only the key of the first row.</p>
     *
     * @param path path for key
     * @return generated keys
     */
    public <T> Flux<T> executeWithKeys(Path<T> path) {
        Mapper<T> mapper = (row, metadata) -> Objects.requireNonNull(row.get(0, path.getType()), "Null key result");
        return requireConnection()
                .map(connection -> createStatement(connection, true))
                .flatMapMany(connection -> executeStatementWithKeys(connection, mapper));
    }

    @Override
    public Mono<Long> execute() {
        if (batchToBulk || batches.isEmpty()) {
            return requireConnection()
                    .map(connection-> createStatement(connection, false))
                    .flatMap(this::executeStatement);
        } else {
            return requireConnection()
                    .map(connection-> createStatement(connection, false))
                    .flatMapMany(this::executeNonBulkBatchStatement)
                    .reduce(0L, Long::sum);
        }
    }

    private Mono<Long> executeStatement(Statement stmt) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private <T> Mono<T> executeStatementWithKey(Statement stmt, Mapper<T> mapper) {
        return Mono.from(stmt.execute())
                .flatMap(result -> Mono.from(result.map(mapper::map)));
    }

    private Flux<Long> executeNonBulkBatchStatement(Statement stmt) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .map(Long::valueOf);
    }

    private <T> Flux<T> executeStatementWithKeys(Statement stmt, Mapper<T> mapper) {
        return Flux.from(stmt.execute())
                .flatMap(result -> Mono.from(result.map(mapper::map)));
    }

    private Statement createStatement(Connection connection, boolean withKeys) {
        SQLSerializer serializer = createSerializerAndSerialize();
        return prepareStatementAndSetParameters(connection, serializer, withKeys);
    }

    private Statement prepareStatementAndSetParameters(Connection connection, SQLSerializer serializer, boolean withKeys) {
        String queryString = serializer.toString();
        queryString = R2dbcUtils.replaceBindingArguments(queryString);
        Statement stmt = connection.createStatement(queryString);
        if (batches.isEmpty()) {
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams(), 0);
        } else {
            int offset = 0;
            for (SQLInsertBatch batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch inserts are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                if (!batchToBulk) {
                    stmt.add();
                } else {
                    offset++;
                }
            }
        }
        if (withKeys) {
            if (entity.getPrimaryKey() != null) {
                String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
                for (int i = 0; i < target.length; i++) {
                    Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
                    String column = ColumnMetadata.getName(path);
                    target[i] = configuration.getTemplates().quoteIdentifier(column);
                }
                stmt.returnGeneratedValues(target);
            }
        }
        return stmt;
    }

    @SuppressWarnings("unchecked")
    private <T> void setBatchParameters(Statement stmt, SQLInsertBatch batch, int offset) {
        Map<ParamExpression<?>,Object> params = new HashMap<>();
        List<Object> constants = batch.getValues()
                .stream()
                .map(c -> ((Constant<T>) c).getConstant()) // TODO: support expressions
                .collect(Collectors.toList());
        setParameters(stmt, constants, batch.getColumns(), params, offset);
    }

    @FunctionalInterface
    private interface Mapper<T> {
        @Nonnull
        T map(Row row, RowMetadata metadata);
    }

}

package com.querydsl.r2dbc.dml;

import com.querydsl.core.*;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.ValidatingVisitor;
import com.querydsl.corereactive.dml.DeleteClause;
import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.internal.R2dbcUtils;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializer;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a base class for dialect-specific DELETE clauses.
 *
 * @param <C> The type extending this class.
 */
public abstract class AbstractR2dbcDeleteClause<C extends AbstractR2dbcDeleteClause<C>> extends AbstractR2dbcClause<C> implements DeleteClause<C> {

    private static final ValidatingVisitor validatingVisitor = new ValidatingVisitor("Undeclared path '%s'. " +
            "A delete operation can only reference a single table. " +
            "Consider this alternative: DELETE ... WHERE EXISTS (subquery)");

    private final RelationalPath<?> entity;

    private final List<QueryMetadata> batches = new ArrayList<>();

    private DefaultQueryMetadata metadata = new DefaultQueryMetadata();

    public AbstractR2dbcDeleteClause(R2dbcConnectionProvider connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration);
        this.entity = entity;
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
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
     * Add current state of bindings as a batch item
     *
     * @return the current object
     */
    public C addBatch() {
        batches.add(metadata);
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
        return self();
    }

    /**
     * Clear the internal state of the clause
     */
    public void clear() {
        batches.clear();
        metadata = new DefaultQueryMetadata();
        metadata.addJoin(JoinType.DEFAULT, entity);
        metadata.setValidatingVisitor(validatingVisitor);
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

    public int getBatchCount() {
        return batches.size();
    }

    private SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    private SQLSerializer createSerializerAndSerialize() {
        SQLSerializer serializer = createSerializer();
        if (!batches.isEmpty()) {
            QueryMetadata first = batches.get(0);
            serializer.serializeDelete(first, entity);
        } else {
            serializer.serializeDelete(metadata, entity);
        }
        return serializer;
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
            for (QueryMetadata batch : batches) {
                if (useLiterals) {
                    throw new UnsupportedOperationException("Batch deletes are not supported with literals");
                }
                setBatchParameters(stmt, batch, offset);
                stmt.add();
            }
        }
        return stmt;
    }

    private <T> void setBatchParameters(Statement stmt, QueryMetadata batch, int offset) {
        SQLSerializer helperSerializer = createSerializer();
        helperSerializer.serializeDelete(batch, entity);
        setParameters(stmt, helperSerializer.getConstants(), helperSerializer.getConstantPaths(), metadata.getParams(), offset);
    }

}

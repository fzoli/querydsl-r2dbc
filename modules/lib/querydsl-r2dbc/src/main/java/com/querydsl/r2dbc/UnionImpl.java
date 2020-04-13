package com.querydsl.r2dbc;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.Query;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

final class UnionImpl<T, Q extends ProjectableR2dbcQuery<T, Q> & Query<Q>> implements Union<T> {

    private final Q query;

    public UnionImpl(Q query) {
        this.query = query;
    }

    @Override
    public Flux<T> fetch() {
        return query.fetch();
    }

    @Override
    public Mono<T> fetchFirst() {
        return query.fetchFirst();
    }

    @Override
    public Mono<T> fetchOne() throws NonUniqueResultException {
        return query.fetchOne();
    }

    @Override
    public Union<T> groupBy(Expression<?>... o) {
        query.groupBy(o);
        return this;
    }

    @Override
    public Union<T> having(Predicate... o) {
        query.having(o);
        return this;
    }

    @Override
    public Union<T> orderBy(OrderSpecifier<?>... o) {
        query.orderBy(o);
        return this;
    }

    @Override
    public Expression<T> as(String alias) {
        return ExpressionUtils.as(this, alias);
    }

    @Override
    public Expression<T> as(Path<T> alias) {
        return ExpressionUtils.as(this, alias);
    }

    @Override
    public String toString() {
        return query.toString();
    }

    @Nullable
    @Override
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return query.accept(v, context);
    }

    @Override
    public Class<? extends T> getType() {
        return query.getType();
    }

    @Override
    public QueryMetadata getMetadata() {
        return query.getMetadata();
    }

}

package com.querydsl.corereactive.support;


import com.querydsl.core.FetchableQuery;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.support.QueryBase;
import com.querydsl.core.support.QueryMixin;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.corereactive.Fetchable;
import reactor.core.publisher.Mono;

/**
 * {@code FetchableQueryBase} extends the {@link QueryBase} class to provide default
 * implementations of the methods of the {@link com.querydsl.corereactive.Fetchable} interface
 *
 * @param <T> result type
 * @param <Q> concrete subtype
 *
 */
public abstract class FetchableQueryBase<T, Q extends FetchableQueryBase<T, Q>>
        extends QueryBase<Q> implements Fetchable<T> {

    public FetchableQueryBase(QueryMixin<Q> queryMixin) {
        super(queryMixin);
    }

    @Override
    public final Mono<T> fetchFirst() {
        return limit(1).fetchOne();
    }

    @Override
    public Mono<T> fetchOne() {
        return fetch().singleOrEmpty();
    }

    public <T> T transform(ResultTransformer<T> transformer) {
        return transformer.transform((FetchableQuery<?,?>) this);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof SubQueryExpression) {
            SubQueryExpression<?> s = (SubQueryExpression<?>) o;
            return s.getMetadata().equals(queryMixin.getMetadata());
        } else {
            return false;
        }
    }

}


package com.querydsl.corereactive.dml;

import reactor.core.publisher.Mono;

/**
 * Parent interface for DML clauses
 *
 * @param <C> concrete subtype
 */
public interface DMLClause<C extends DMLClause<C>> {

    /**
     * Execute the clause and return the amount of affected rows
     *
     * @return amount of affected rows or empty if not available
     */
    Mono<Long> execute();

}

package com.querydsl.corereactive.dml;

import com.querydsl.core.types.Predicate;

/**
 * {@code FilteredClause} is an interface for clauses with a filter condition
 *
 * @param <C> concrete subtype
 */
public interface FilteredClause<C extends FilteredClause<C>> {

    /**
     * Adds the given filter conditions
     *
     * <p>Skips null arguments</p>
     *
     * @param o filter conditions to be added
     * @return the current object
     */
    C where(Predicate... o);

}

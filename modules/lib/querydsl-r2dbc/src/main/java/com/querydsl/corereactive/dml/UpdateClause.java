package com.querydsl.corereactive.dml;

import java.util.List;

import com.querydsl.core.types.Path;

/**
 * {@code UpdateClause} defines a generic extensible interface for Update clauses
 *
 * @param <C> concrete subtype
 */
public interface UpdateClause<C extends UpdateClause<C>> extends StoreClause<C>, FilteredClause<C> {

    /**
     * Set the paths to be updated
     *
     * @param paths paths to be updated
     * @param values values to be set
     * @return the current object
     */
    C set(List<? extends Path<?>> paths, List<?> values);

}

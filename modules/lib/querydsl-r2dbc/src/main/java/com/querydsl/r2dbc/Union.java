package com.querydsl.r2dbc;

import com.querydsl.core.types.*;
import com.querydsl.corereactive.Fetchable;

/**
 * {@link Union} defines an interface for Union queries
 *
 * @param <RT> return type of projection
 */
public interface Union<RT> extends SubQueryExpression<RT>, Fetchable<RT> {

    /**
     * Defines the grouping/aggregation expressions
     *
     * @param o group by
     * @return the current object
     */
    Union<RT> groupBy(Expression<?>... o);

    /**
     * Defines the filters for aggregation
     *
     * @param o having conditions
     * @return the current object
     */
    Union<RT> having(Predicate... o);


    /**
     * Define the ordering of the query results
     *
     * @param o order
     * @return the current object
     */
    Union<RT> orderBy(OrderSpecifier<?>... o);

    /**
     * Create an alias for the expression
     *
     * @param alias alias
     * @return this as alias
     */
    Expression<RT> as(String alias);

    /**
     * Create an alias for the expression
     *
     * @param alias alias
     * @return this as alias
     */
    Expression<RT> as(Path<RT> alias);

}

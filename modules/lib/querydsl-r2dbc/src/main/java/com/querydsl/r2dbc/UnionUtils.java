package com.querydsl.r2dbc;

import com.querydsl.core.types.*;
import com.querydsl.sql.SQLOps;

import java.util.List;

/**
 * UnionUtils provides static utility methods for Union handling.
 */
final class UnionUtils {

    public static <T> Expression<T> union(List<SubQueryExpression<T>> union, boolean unionAll) {
        final Operator operator = unionAll ? SQLOps.UNION_ALL : SQLOps.UNION;
        Expression<T> rv = union.get(0);
        for (int i = 1; i < union.size(); i++) {
            rv = ExpressionUtils.operation(rv.getType(), operator, rv, union.get(i));
        }
        return rv;
    }

    public static <T> Expression<T> union(List<SubQueryExpression<T>> union, Path<T> alias,
            boolean unionAll) {
        final Expression<T> rv = union(union, unionAll);
        return ExpressionUtils.as(rv, alias);
    }

    private UnionUtils() { }

}

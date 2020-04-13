package com.querydsl.r2dbc.internal;

public class R2dbcUtils {

    private R2dbcUtils() {
    }

    public static String replaceBindingArguments(String originalSql) {
        String sql = originalSql;
        int counter = 1;
        for (;;) {
            int index = sql.indexOf('?');
            if (index == -1) {
                break;
            }
            String first = sql.substring(0, index);
            String second = sql.substring(index + 1);
            sql = first + "$" + counter + second;
            counter++;
        }
        return sql;
    }

}

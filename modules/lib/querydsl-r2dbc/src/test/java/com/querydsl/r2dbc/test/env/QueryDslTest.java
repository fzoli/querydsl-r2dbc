package com.querydsl.r2dbc.test.env;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@ExtendWith(QueryDslTestExtension.class)
public @interface QueryDslTest {
}

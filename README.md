# QueryDSL R2DBC support

## Introduction

This project is based on [QueryDSL SQL Support](https://mvnrepository.com/artifact/com.querydsl/querydsl-sql/4.3.1).

It supports the following databases:
- PostgreSQL - see `com.querydsl.r2dbc.postgresql.PostgreSqlR2dbcQueryFactory`
- MySQL - see `com.querydsl.r2dbc.mysql.MySqlR2dbcQueryFactory`

There is a [feature request](https://github.com/querydsl/querydsl/issues/2468) to support R2DBC.
This project is just a reference implementation to help the QueryDSL team.

The implementation can be improved by the QueryDSL team as they can modify their code.
90% of the source code is just a copy-paste of the SQL support.

## Test

The JUnit tests can run on any machine that has Docker installed.
The first run downloads Docker image `postgres:12.2` as it is required to test the implementation on a real database.

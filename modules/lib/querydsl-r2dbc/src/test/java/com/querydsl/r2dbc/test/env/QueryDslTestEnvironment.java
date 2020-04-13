package com.querydsl.r2dbc.test.env;

import com.querydsl.r2dbc.test.env.database.DatabaseSystem;
import com.querydsl.r2dbc.test.env.factory.JdbcConnectionFactory;
import com.querydsl.r2dbc.test.env.factory.R2dbcConnectionFactory;
import com.querydsl.r2dbc.test.env.runner.TestRunner;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class QueryDslTestEnvironment {

    private static final QueryDslTestEnvironment instance = new QueryDslTestEnvironment();

    static QueryDslTestEnvironment getInstance() {
        return instance;
    }

    public static void run(Consumer<TestRunner.Environment> block) {
        getInstance().runBlock(block);
    }

    private Lock lock = new ReentrantLock();
    private DatabaseSystem database;
    private TestRunner testRunner;

    private QueryDslTestEnvironment() {
    }

    private void runBlock(Consumer<TestRunner.Environment> block) {
        testRunner.run(block);
    }

    public void prepareTest() {
        startOnce();
    }

    private void startOnce() {
        lock.lock();
        try {
            if (this.database == null) {
                DatabaseSystem database = new DatabaseSystem().startAndStopBeforeExit();
                this.testRunner = new TestRunner(
                        new JdbcConnectionFactory(database),
                        new R2dbcConnectionFactory(database)
                );
                this.database = database;
            }
        } finally {
            lock.unlock();
        }
    }

}

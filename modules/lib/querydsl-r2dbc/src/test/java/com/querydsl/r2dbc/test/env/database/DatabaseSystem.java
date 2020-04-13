package com.querydsl.r2dbc.test.env.database;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class DatabaseSystem {

    private static final Supplier<PostgreSqlDatabaseDockerContainer> POSTGRES_CONTAINER_SUPPLIER = () ->
            new PostgreSqlDatabaseDockerContainer("postgres:12.2")
                    .withDatabaseName("db")
                    .withUsername("test")
                    .withPassword("test")
                    .withStartupTimeout(Duration.ofSeconds(60))
                    .withMaxConnectionLimit(500)
            ;

    private final Lock lock = new ReentrantLock();
    private PostgreSqlDatabaseDockerContainer container;

    public String getJdbcUrl() {
        return requireContainer().getJdbcUrl();
    }

    public String getHost() {
        return requireContainer().getContainerIpAddress();
    }

    public int getPort() {
        return requireContainer().getPort();
    }

    public String getDatabaseName() {
        return requireContainer().getDatabaseName();
    }

    public String getUsername() {
        return requireContainer().getUsername();
    }

    public String getPassword() {
        return requireContainer().getPassword();
    }

    private PostgreSqlDatabaseDockerContainer requireContainer() {
        if (container == null) {
            throw new IllegalStateException("Database is not started");
        }
        return container;
    }

    public DatabaseSystem startAndStopBeforeExit() {
        lock.lock();
        try {
            PostgreSqlDatabaseDockerContainer container = POSTGRES_CONTAINER_SUPPLIER.get();
            container.start();
            this.container = container;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (container.isRunning()) {
                    container.stop();
                }
            }));
            return this;
        } finally {
            lock.unlock();
        }
    }

}

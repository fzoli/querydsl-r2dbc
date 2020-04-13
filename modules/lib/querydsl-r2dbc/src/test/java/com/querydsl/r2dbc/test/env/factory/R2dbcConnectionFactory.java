package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.r2dbc.test.env.database.DatabaseSystem;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class R2dbcConnectionFactory {

    private final ConnectionFactory delegate;

    public R2dbcConnectionFactory(@Nonnull DatabaseSystem databaseSystem) {
        delegate = createConnectionFactory(databaseSystem);
    }

    public Mono<Connection> createConnection() {
        return Mono.from(delegate.create());
    }

    private ConnectionFactory createConnectionFactory(@Nonnull DatabaseSystem databaseSystem) {
        return ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                        .option(ConnectionFactoryOptions.HOST, databaseSystem.getHost())
                        .option(ConnectionFactoryOptions.PORT, databaseSystem.getPort())
                        .option(ConnectionFactoryOptions.USER, databaseSystem.getUsername())
                        .option(ConnectionFactoryOptions.PASSWORD, databaseSystem.getPassword())
                        .option(ConnectionFactoryOptions.DATABASE, databaseSystem.getDatabaseName())
                        .build()
        );
    }

}

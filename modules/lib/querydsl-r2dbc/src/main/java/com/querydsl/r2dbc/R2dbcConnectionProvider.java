package com.querydsl.r2dbc;

import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface R2dbcConnectionProvider {

    /**
     * Returns the active connection of the current transaction.
     * Does not create new connection. Consuming the result outside of a transaction throws exception.
     * @return the connection of the current transaction
     */
    Mono<Connection> getConnection();

}

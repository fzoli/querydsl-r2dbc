package com.querydsl.r2dbc.test.env.database;

import org.testcontainers.containers.PostgreSQLContainer;

class PostgreSqlDatabaseDockerContainer extends PostgreSQLContainer<PostgreSqlDatabaseDockerContainer> {

    private boolean maxLimitSet = false;

    public PostgreSqlDatabaseDockerContainer(String dockerImageName) {
        super(dockerImageName);

    }

    public PostgreSqlDatabaseDockerContainer withMaxConnectionLimit(int value) {
        if (maxLimitSet) {
            throw new IllegalStateException("Called multiple times");
        }
        addParameter("-N", Integer.toString(value));
        maxLimitSet = true;
        return this;
    }

    public int getPort() {
        return getMappedPort(POSTGRESQL_PORT);
    }

}

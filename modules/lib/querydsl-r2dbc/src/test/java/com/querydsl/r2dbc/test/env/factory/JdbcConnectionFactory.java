package com.querydsl.r2dbc.test.env.factory;

import com.querydsl.r2dbc.test.env.database.DatabaseSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnectionFactory {

    private final DatabaseSystem databaseSystem;

    public JdbcConnectionFactory(DatabaseSystem databaseSystem) {
        this.databaseSystem = databaseSystem;
    }

    public Connection createConnection() {
        try {
            return DriverManager.getConnection(
                    databaseSystem.getJdbcUrl(),
                    databaseSystem.getUsername(), databaseSystem.getPassword()
            );
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}

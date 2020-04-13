package com.querydsl.r2dbc.test.env.runner;

import com.querydsl.r2dbc.R2dbcConnectionProvider;
import com.querydsl.r2dbc.FixedR2dbcConnectionProvider;
import com.querydsl.r2dbc.R2dbcQuery;
import com.querydsl.r2dbc.dml.R2dbcDeleteClause;
import com.querydsl.r2dbc.dml.R2dbcInsertClause;
import com.querydsl.r2dbc.dml.R2dbcUpdateClause;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.r2dbc.test.env.factory.*;
import io.r2dbc.spi.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;

public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    private final JdbcConnectionFactory jdbcConnectionFactory;
    private final R2dbcConnectionFactory connectionFactory;

    public TestRunner(JdbcConnectionFactory jdbcConnectionFactory, R2dbcConnectionFactory connectionFactory) {
        this.jdbcConnectionFactory = jdbcConnectionFactory;
        this.connectionFactory = connectionFactory;
    }

    /**
     * This is not a transaction.
     * The block is able to access the initiated, clean database using both JDBC and R2DBC world.
     */
    public void run(Consumer<Environment> block) {
        java.sql.Connection jdbcConnection = jdbcConnectionFactory.createConnection();
        Connection connection = Objects.requireNonNull(connectionFactory.createConnection().block());
        Environment environment = new Environment(jdbcConnection, connection);
        SchemaAlter schema = new SchemaAlter(environment);
        try {
            schema.create();
            block.accept(environment);
        } finally {
            schema.drop();
            tryClose(connection);
            tryClose(jdbcConnection);
        }
    }

    private void tryClose(java.sql.Connection jdbcConnection) {
        try {
            jdbcConnection.close();
        } catch (SQLException ex) {
            logger.error("Failed to close the JDBC connection", ex);
        }
    }

    private void tryClose(Connection connection) {
        try {
            Mono.from(connection.close()).block();
        } catch (RuntimeException ex) {
            logger.error("Failed to close the R2DBC connection", ex);
        }
    }

    public static class Environment {

        private final R2dbcQueryFactory queryFactory;
        private final R2dbcClauseFactory clauseFactory;

        private final JdbcQueryFactory jdbcQueryFactory;
        private final JdbcClauseFactory jdbcClauseFactory;
        private final java.sql.Connection jdbcConnection;

        private Environment(java.sql.Connection jdbcConnection, Connection connection) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            R2dbcConnectionProvider connectionProvider = FixedR2dbcConnectionProvider.of(connection);
            this.queryFactory = new R2dbcQueryFactory(configurationFactory, connectionProvider);
            this.clauseFactory = new R2dbcClauseFactory(configurationFactory, connectionProvider);
            this.jdbcQueryFactory = new JdbcQueryFactory(configurationFactory, jdbcConnection);
            this.jdbcClauseFactory = new JdbcClauseFactory(configurationFactory, jdbcConnection);
            this.jdbcConnection = jdbcConnection;
        }

        public void executeStatement(String sql) {
            try (PreparedStatement statement = jdbcConnection.prepareStatement(sql)) {
                statement.execute();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        public R2dbcQuery<Object> query() {
            // This class is under test.
            return queryFactory.createQuery();
        }

        public R2dbcInsertClause insert(RelationalPath<?> entity) {
            // This class is under test.
            return clauseFactory.createInsertClause(entity);
        }

        public R2dbcUpdateClause update(RelationalPath<?> entity) {
            // This class is under test.
            return clauseFactory.createUpdateClause(entity);
        }

        public R2dbcDeleteClause delete(RelationalPath<?> entity) {
            // This class is under test.
            return clauseFactory.createDeleteClause(entity);
        }

        public SQLQuery<Object> jdbcQuery() {
            // Already tested class.
            return jdbcQueryFactory.createQuery();
        }

        public SQLInsertClause jdbcInsert(RelationalPath<?> entity) {
            // Already tested class.
            return jdbcClauseFactory.insert(entity);
        }

    }

}

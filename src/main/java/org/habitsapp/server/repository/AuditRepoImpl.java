package org.habitsapp.server.repository;

import org.habitsapp.exchange.AuditEventDto;
import org.habitsapp.server.migration.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

@Component
@DependsOn("migration")
public class AuditRepoImpl implements AuditRepo {
    private final Logger logger = LoggerFactory.getLogger(AuditRepoImpl.class);

    private final String DB_URL;
    private final String DB_USER_NAME;
    private final String DB_PASSWORD;
    private final String LOG_SCHEMA_NAME;
    private final String TBL_LOG_NAME;

    public AuditRepoImpl(DatabaseConfig dbConfig) {
        DB_URL = dbConfig.getUrl();
        DB_USER_NAME = dbConfig.getUsername();
        DB_PASSWORD = dbConfig.getPassword();
        LOG_SCHEMA_NAME = dbConfig.getLogSchemaName();
        TBL_LOG_NAME = dbConfig.getTblLogName();
    }

    private void handleSQLException(SQLException e) {
        logger.error("HandleSQLException: {}", e.getMessage());
    }

    public void saveToLog(Long userId, String message) {
        String QUERY_SAVE_MSG = String.format(
                "INSERT INTO %s.%s (\"user_id\", \"message\", \"timestamp\")" +
                " VALUES (?, ?, ?);",
                LOG_SCHEMA_NAME, TBL_LOG_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_MSG)) {
            if (userId == null) {
                    pStatement.setNull(1, Types.BIGINT);
            } else {
                pStatement.setLong(1, userId);
            }
            pStatement.setString(2, message);
            pStatement.setTimestamp(3, Timestamp.from(Instant.now()));
            pStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public List<AuditEventDto> getFromLog(Long userId, int limit, int offset) {
        String QUERY_SAVE_MSG = String.format(
                "SELECT (\"user_id\", \"message\", \"timestamp\") FROM %s.%s" +
                " WHERE (user_id = ? OR ? = -1)" +
                " LIMIT ? OFFSET ?;",
                LOG_SCHEMA_NAME, TBL_LOG_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_MSG)) {

            pStatement.setLong(1, userId != null ? userId : -1);
            pStatement.setLong(2, userId != null ? userId : -1);
            pStatement.setInt(3, limit);
            pStatement.setInt(4, offset);
            ResultSet resultSet = pStatement.executeQuery();

            List<AuditEventDto> logs = new LinkedList<>();
            while (resultSet.next()) {
                logs.add(new AuditEventDto(resultSet.getLong("user_id"),
                        resultSet.getString("message"),
                        resultSet.getTimestamp("timestamp").toInstant()));
            }
            return logs;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return Collections.emptyList();
    }

}

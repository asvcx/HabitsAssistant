package org.habitsapp.server.repository;

import org.habitsapp.models.EntityStatus;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.dbmappers.DBHabitMapper;
import org.habitsapp.server.repository.dbmappers.ResultSetMapper;
import org.habitsapp.server.repository.dbmappers.DBUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * The Database class provides methods for interacting with the database,
 * including loading, saving, updating, and deleting user and habit data.
 */
@Component
public class DatabasePostgres implements Database {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePostgres.class);

    private final String DB_URL;
    private final String DB_USER_NAME;
    private final String DB_PASSWORD;
    private final String SCHEMA_NAME;
    private final String TBL_USERS_NAME;
    private final String TBL_HABITS_NAME;
    private final String TBL_DATES_NAME;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Failed to load PostgreSQL driver: " + e.getMessage());
        }
    }

    /**
     * Create a Database instance with the specified connection parameters.
     */
    @Autowired
    public DatabasePostgres(DatabaseConfig dbConfig) {
        DB_URL = dbConfig.getUrl();
        DB_USER_NAME = dbConfig.getUsername();
        DB_PASSWORD = dbConfig.getPassword();
        SCHEMA_NAME = dbConfig.getSchemaName();
        TBL_USERS_NAME = dbConfig.getTblUsersName();
        TBL_HABITS_NAME = dbConfig.getTblHabitsName();
        TBL_DATES_NAME = dbConfig.getTblDatesName();
    }

    private void handleSQLException(SQLException e) {
        logger.error("HandleSQLException: {}", e.getMessage());
    }

    /**
     * Execute the given SQL query and maps the result set to a list of objects.
     */
    private <T> List<T> executeQuery(String query, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                results.add(mapper.mapToObj(resultSet));
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return results;
    }

    /**
     * Load users from the database.
     *
     * @return a list of users loaded from the database
     */
    public List<User> loadUsers() {
        String QUERY_LOAD_USERS = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_USERS_NAME);
        List<User> users = executeQuery(QUERY_LOAD_USERS, new DBUserMapper());
        for (User user : users) {
            logger.info("User loaded from database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
        }
        return users;
    }

    /**
     * Load habits from the database, grouped by user ID.
     *
     * @return a map where the key is the user ID and the value is a list of habits
     */
    public Map<Long,List<Habit>> loadHabits() {
        String QUERY_LOAD_HABITS = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_HABITS_NAME);
        List<Habit> habits = executeQuery(QUERY_LOAD_HABITS, new DBHabitMapper());
        Map<Long,List<Habit>> habitsByUserID = new HashMap<>();
        for (Habit habit : habits) {
            long userID = habit.getUserId();
            loadDates(userID, habit);
            habitsByUserID.computeIfAbsent(userID, _ -> new ArrayList<>()).add(habit);
        }
        return habitsByUserID;
    }

    /**
     * Load completion dates for specified habit.
     */
    private void loadDates(long userID, Habit habit) {
        String QUERY_LOAD_DATES = String.format("SELECT * FROM %s.%s WHERE \"UserID\" = ? AND \"Title\" = ?;", SCHEMA_NAME, TBL_DATES_NAME);
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            PreparedStatement pStatement = connection.prepareStatement(QUERY_LOAD_DATES);
            pStatement.setLong(1, userID);
            pStatement.setString(2, habit.getTitle());
            try (ResultSet resultSet = pStatement.executeQuery()) {
                while(resultSet.next()) {
                    Instant date = resultSet.getTimestamp("CompletionDate").toInstant();
                    habit.addCompletionDate(date);
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        }  catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Save a list of users to the database.
     */
    public void saveUsers(List<User> users) {
        String QUERY_SAVE_USER = String.format(
                "INSERT INTO %s.%s (\"UserID\", \"UserName\", \"Email\", \"Password\", \"Blocked\", \"AccessLevel\")" +
                " VALUES (nextval('habits_model_schema.user_seq'), ?, ?, ?, ?, ?)" +
                " ON CONFLICT (\"UserID\") DO NOTHING RETURNING \"UserID\";",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_USER)) {
            for (User user : users) {
                if (user.getAccountStatus() == EntityStatus.CREATED) {
                    new DBUserMapper().mapFromObj(pStatement, user);
                    ResultSet resultSet = pStatement.executeQuery();
                    resultSet.next();
                    long userId = resultSet.getLong("UserID");
                    user.setId(userId);
                    user.setAccountStatus(EntityStatus.STABLE);
                    logger.info("User saved to database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Save a list of habits to the database.
     */
    public void saveHabits(long userID, List<Habit> habits) {
        String QUERY_SAVE_HABIT = String.format(
                "INSERT INTO %s.%s (\"HabitID\", \"UserID\", \"Title\", \"Description\", \"Period\", \"StartDate\")" +
                " VALUES (nextval('habits_model_schema.habit_seq'), ?, ?, ?, ?, ?)" +
                " ON CONFLICT (\"HabitID\") DO NOTHING;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_HABIT)) {
            for (Habit habit : habits) {
                if (habit.getStatus() == EntityStatus.CREATED) {
                    new DBHabitMapper().mapFromObj(pStatement, habit);
                    pStatement.executeUpdate();
                    saveDates(connection, userID, habit);
                    habit.setStatus(EntityStatus.STABLE);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Save completion dates of specified habit.
     */
    private void saveDates(Connection connection, long userID, Habit habit) {
        String QUERY_SAVE_DATE = String.format(
                "INSERT INTO %s.%s (\"UserID\", \"Title\", \"CompletionDate\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"UserID\", \"Title\", \"CompletionDate\") DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME
        );
        try (PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_DATE)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userID);
                pStatement.setString(2, habit.getTitle());
                pStatement.setTimestamp(3, Timestamp.from(date));
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Update users with status 'UPDATED'.
     */
    public void updateUsers(List<User> users) {
        String QUERY_UPDATE_USER = String.format(
                "UPDATE %s.%s SET \"UserName\" = ?, \"Email\" = ?, \"Password\" = ?, \"Blocked\" = ?, \"AccessLevel\" = ?" +
                " WHERE \"UserID\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_UPDATE_USER)) {
            for (User user : users) {
                if (user.getAccountStatus() == EntityStatus.UPDATED) {
                    new DBUserMapper().mapFromObj(pStatement, user);
                    pStatement.setLong(6, user.getId());
                    pStatement.executeUpdate();
                    user.setAccountStatus(EntityStatus.STABLE);
                    logger.info("User updated in database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Update habits with status 'UPDATED' of a specified user.
     */
    public void updateHabits(long userID, List<Habit> habits) {
        String QUERY_UPDATE_HABIT = String.format(
                "UPDATE %s.%s SET \"UserID\" = ?, \"Title\" = ?, \"Description\" = ?, \"Period\" = ?, \"StartDate\" = ?" +
                " WHERE \"HabitID\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_UPDATE_HABIT)) {
            for (Habit habit : habits) {
                if (habit.getStatus() == EntityStatus.UPDATED) {
                    new DBHabitMapper().mapFromObj(pStatement, habit);
                    pStatement.setInt(6, habit.getId());
                    pStatement.executeUpdate();
                    saveDates(connection, userID, habit);
                    habit.setStatus(EntityStatus.STABLE);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Remove users from database whose status field is 'DELETED'
     */
    public void removeUsers(List<User> users) {
        String QUERY_REMOVE_USER = String.format(
                "DELETE FROM %s.%s WHERE \"UserID\" = ? AND \"Email\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_USER)) {
                for (User user : users) {
                    if (user.getAccountStatus() == EntityStatus.DELETED) {
                        pStatement.setLong(1, user.getId());
                        pStatement.setString(2, user.getEmail());
                        pStatement.executeUpdate();
                        logger.info("User deleted from database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
                    }
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Remove habits from database with status field set to 'DELETED'
     */
    public void removeHabits(long userID, List<Habit> habits) {
        String QUERY_REMOVE_HABIT = String.format(
                "DELETE FROM %s.%s WHERE \"HabitID\" = ? AND \"UserEmail\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_HABIT)) {
                for (Habit habit : habits) {
                    if (habit.getStatus() == EntityStatus.DELETED) {
                        removeDates(connection, userID, habit);
                        pStatement.setInt(1, habit.getId());
                        pStatement.setLong(2, userID);
                        pStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Remove completion dates from database for specified habit
     */
    private void removeDates(Connection connection, long userID, Habit habit) {
        String QUERY_REMOVE_DATE = String.format(
                "DELETE FROM %s.%s WHERE \"UserID\" = ? AND \"Title\" = ? AND \"CompletionDate\" = ?;",
                SCHEMA_NAME, TBL_DATES_NAME
        );
        try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_DATE)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userID);
                pStatement.setString(2, habit.getTitle());
                pStatement.setTimestamp(3, Timestamp.from(date));
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

}
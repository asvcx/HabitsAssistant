package org.habitsapp.server.repository;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.dbmapper.DBHabitMapper;
import org.habitsapp.server.repository.dbmapper.ResultSetMapper;
import org.habitsapp.server.repository.dbmapper.DBUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * The Database class provides methods for interacting with the database,
 * including loading, saving, updating, and deleting user and habit data.
 */
@Component
@DependsOn("migration")
public class DatabasePostgres implements Database {
    private static final Logger logger = LoggerFactory.getLogger(DatabasePostgres.class);

    private final String DB_URL;
    private final String DB_USER_NAME;
    private final String DB_PASSWORD;
    private final String SCHEMA_NAME;
    private final String TBL_USERS_NAME;
    private final String TBL_HABITS_NAME;
    private final String TBL_DATES_NAME;

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

    public List<User> loadUsers() {
        String QUERY_LOAD_USERS = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_USERS_NAME);
        return executeQuery(QUERY_LOAD_USERS, new DBUserMapper());
    }

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

    public Optional<User> loadUser(long id) {
        return loadUserByQuery("SELECT * FROM %s.%s WHERE \"user_id\" = ?;", id);
    }

    public Optional<User> loadUser(String email) {
        return loadUserByQuery("SELECT * FROM %s.%s WHERE \"email\" = ?;", email);
    }

    /**
     * Load user with specified id or email from the database.
     *
     * @return an Optional of the User
     */
    private Optional<User> loadUserByQuery(String queryTemplate, Object param) {
        String query = String.format(queryTemplate, SCHEMA_NAME, TBL_USERS_NAME);
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
            PreparedStatement pStatement = connection.prepareStatement(query)) {
            if (param instanceof Long) {
                pStatement.setLong(1, (Long) param);
            } else if (param instanceof String) {
                pStatement.setString(1, (String) param);
            }
            try (ResultSet resultSet = pStatement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new DBUserMapper().mapToObj(resultSet);
                    logger.info("User loaded from database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        }  catch (SQLException e) {
            handleSQLException(e);
        }
        return Optional.empty();
    }

    /**
     * Load habits for user with specified id.
     *
     * @return an Optional of User
     */
    public Map<String,Habit> loadHabits(long userId) {
        String QUERY_LOAD_HABITS = String.format("SELECT * FROM %s.%s WHERE \"user_id\" = ?;", SCHEMA_NAME, TBL_HABITS_NAME);
        Map<String,Habit> habits = new HashMap<>();
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            PreparedStatement pStatement = connection.prepareStatement(QUERY_LOAD_HABITS);
            pStatement.setLong(1, userId);
            try (ResultSet resultSet = pStatement.executeQuery()) {
                while (resultSet.next()) {
                    Habit habit = new DBHabitMapper().mapToObj(resultSet);
                    loadDates(userId, habit);
                    habits.put(habit.getTitle(), habit);
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        }  catch (SQLException e) {
            handleSQLException(e);
        }
        return habits;
    }

    /**
     * Load completion dates for specified habit.
     */
    private void loadDates(long userId, Habit habit) {
        String QUERY_LOAD_DATES = String.format("SELECT * FROM %s.%s WHERE \"user_id\" = ? AND \"title\" = ?;", SCHEMA_NAME, TBL_DATES_NAME);
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            PreparedStatement pStatement = connection.prepareStatement(QUERY_LOAD_DATES);
            pStatement.setLong(1, userId);
            pStatement.setString(2, habit.getTitle());
            try (ResultSet resultSet = pStatement.executeQuery()) {
                while(resultSet.next()) {
                    Instant date = resultSet.getTimestamp("completion_date").toInstant();
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
     * Save a user to the database.
     */
    public boolean saveUser(User user) {
        String QUERY_SAVE_USER = String.format(
                "INSERT INTO %s.%s (\"user_id\", \"user_name\", \"email\", \"password\", \"blocked\", \"access_level\")" +
                        " VALUES (nextval('habits_model_schema.user_seq'), ?, ?, ?, ?, ?)" +
                        " ON CONFLICT (\"user_id\") DO NOTHING RETURNING \"user_id\";",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_USER)) {
            new DBUserMapper().mapFromObj(pStatement, user);
            ResultSet resultSet = pStatement.executeQuery();
            resultSet.next();
            long userId = resultSet.getLong("user_id");
            user.setId(userId);
            logger.info("User saved to database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Save a habit to the database.
     */
    public boolean saveHabit(long userId, Habit habit) {
        String QUERY_SAVE_HABIT = String.format(
                "INSERT INTO %s.%s (\"habit_id\", \"user_id\", \"title\", \"description\", \"period\", \"start_date\")" +
                        " VALUES (nextval('habits_model_schema.habit_seq'), ?, ?, ?, ?, ?)" +
                        " ON CONFLICT (\"habit_id\") DO NOTHING;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_HABIT)) {
            habit.setUserId(userId);
            new DBHabitMapper().mapFromObj(pStatement, habit);
            int result = pStatement.executeUpdate();
            saveDates(connection, habit.getUserId(), habit);
            return result > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Save completion dates of specified habit.
     */
    private void saveDates(Connection connection, long userId, Habit habit) {
        String QUERY_SAVE_DATE = String.format(
                "INSERT INTO %s.%s (\"user_id\", \"title\", \"completion_date\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"user_id\", \"title\", \"completion_date\") DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME
        );
        try (PreparedStatement pStatement = connection.prepareStatement(QUERY_SAVE_DATE)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userId);
                pStatement.setString(2, habit.getTitle());
                pStatement.setTimestamp(3, Timestamp.from(date));
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Update a user with status 'UPDATED'.
     */
    public boolean updateUser(User user) {
        String QUERY_UPDATE_USER = String.format(
                "UPDATE %s.%s SET \"user_name\" = ?, \"email\" = ?, \"password\" = ?, \"blocked\" = ?, \"access_level\" = ?" +
                        " WHERE \"user_id\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_UPDATE_USER)) {
            new DBUserMapper().mapFromObj(pStatement, user);
            pStatement.setLong(6, user.getId());
            int result = pStatement.executeUpdate();
            logger.info("User updated in database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
            return result > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Update a habit with status 'UPDATED' of a specified user.
     */
    public boolean updateHabit(long userId, Habit habit) {
        String QUERY_UPDATE_HABIT = String.format(
                "UPDATE %s.%s SET \"user_id\" = ?, \"title\" = ?, \"description\" = ?, \"period\" = ?, \"startDate\" = ?" +
                        " WHERE \"habit_id\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);
             PreparedStatement pStatement = connection.prepareStatement(QUERY_UPDATE_HABIT)) {
            new DBHabitMapper().mapFromObj(pStatement, habit);
            pStatement.setInt(6, habit.getId());
            int result = pStatement.executeUpdate();
            saveDates(connection, userId, habit);
            return result > 0;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Remove a user from database whose status field is 'DELETED'
     */
    public boolean removeUser(User user) {
        String QUERY_REMOVE_USER = String.format(
                "DELETE FROM %s.%s WHERE \"user_id\" = ? AND \"email\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_USER)) {
                pStatement.setLong(1, user.getId());
                pStatement.setString(2, user.getEmail());
                int result = pStatement.executeUpdate();
                if (result > 0) {
                    logger.info("User deleted from database. Email : [{}]; id : [{}]", user.getEmail(), user.getId());
                    return true;
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Remove a user from database whose status field is 'DELETED'
     */
    public boolean removeUser(long id, String email) {
        String QUERY_REMOVE_USER = String.format(
                "DELETE FROM %s.%s WHERE \"user_id\" = ? AND \"email\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_USER)) {
                pStatement.setLong(1, id);
                pStatement.setString(2, email.toLowerCase());
                int result = pStatement.executeUpdate();
                if (result > 0) {
                    logger.info("User deleted from database. Email : [{}]; id : [{}]", email.toLowerCase(), id);
                    return true;
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Remove a habit by user id and habit id from database
     */
    public boolean removeHabit(long userId, int habitId) {
        String QUERY_REMOVE_HABIT = String.format(
                "DELETE FROM %s.%s WHERE \"habit_id\" = ? AND \"user_id\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_HABIT)) {
                removeDates(connection, userId, habitId);
                pStatement.setInt(1, habitId);
                pStatement.setLong(2, userId);
                return pStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Remove a habit by user id and habit title from database
     */
    public boolean removeHabit(long userId, String title) {
        String QUERY_REMOVE_HABIT = String.format(
                "DELETE FROM %s.%s WHERE \"title\" = ? AND \"user_id\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME
        );
        try(Connection connection = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD)) {
            try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_HABIT)) {
                removeDates(connection, userId, title);
                pStatement.setString(1, title);
                pStatement.setLong(2, userId);
                return pStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    /**
     * Remove completion dates from database for specified habit title
     */
    private void removeDates(Connection connection, long userId, String title) {
        String QUERY_REMOVE_DATE = String.format(
                "DELETE FROM %s.%s WHERE \"user_id\" = ? AND \"title\" = ?;",
                SCHEMA_NAME, TBL_DATES_NAME
        );
        try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_DATE)) {
            pStatement.setLong(1, userId);
            pStatement.setString(2, title);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Remove completion dates from database for specified habit id
     */
    private void removeDates(Connection connection, long userId, int habitId) {
        String QUERY_REMOVE_DATE = String.format(
                "DELETE FROM %s.%s WHERE \"user_id\" = ? AND \"habit_id\" = ?;",
                SCHEMA_NAME, TBL_DATES_NAME
        );
        try (PreparedStatement pStatement = connection.prepareStatement(QUERY_REMOVE_DATE)) {
            pStatement.setLong(1, userId);
            pStatement.setLong(2, habitId);
            pStatement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

}
package habitsapp.data.database;

import habitsapp.data.database.mappers.HabitMapper;
import habitsapp.data.database.mappers.ResultSetMapper;
import habitsapp.data.database.mappers.UserMapper;
import habitsapp.data.models.EntityStatus;
import habitsapp.data.models.Habit;
import habitsapp.data.models.User;
import java.sql.*;
import java.time.Instant;
import java.util.*;

/**
 * The Database class provides methods for interacting with the database,
 * including loading, saving, updating, and deleting user and habit data.
 */
public class DatabasePostgres implements Database {
    private static final String SCHEMA_NAME = "habits_model_schema";
    private static final String TBL_USERS_NAME = "users";
    private static final String TBL_HABITS_NAME = "habits";
    private static final String TBL_DATES_NAME = "completion_dates";
    private static String dbUrl;
    private static String dbUserName;
    private static String dbPassword;

    private void handleSQLException(SQLException e) {
        System.out.println("Exception: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Creates a Database instance with the specified connection parameters.
     */
    public DatabasePostgres(String url, String name, String password) {
        dbUrl = url;
        dbUserName = name;
        dbPassword = password;
    }

    /**
     * Executes the given SQL query and maps the result set to a list of objects.
     */
    private <T> List<T> executeQuery(String query, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
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
     * Loads users from the database.
     *
     * @return a list of users loaded from the database
     */
    public List<User> loadUsers() {
        String query = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_USERS_NAME);
        return executeQuery(query, new UserMapper());
    }

    /**
     * Loads habits from the database, grouped by user ID.
     *
     * @return a map where the key is the user ID and the value is a list of habits
     */
    public Map<Long,List<Habit>> loadHabits() {
        String query = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_HABITS_NAME);
        List<Habit> habits = executeQuery(query, new HabitMapper());
        Map<Long,List<Habit>> habitsByUserID = new HashMap<>();
        for (Habit habit : habits) {
            Long userID = habit.getUserID();
            loadDates(userID, habit);
            habitsByUserID.computeIfAbsent(userID, _ -> new ArrayList<>()).add(habit);
        }
        return habitsByUserID;
    }

    /**
     * Loads completion dates for specified habit.
     */
    private void loadDates(long userID, Habit habit) {
        String query = String.format("SELECT * FROM %s.%s WHERE \"UserID\" = ? AND \"Title\" = ?;", SCHEMA_NAME, TBL_DATES_NAME);
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            PreparedStatement pStatement = connection.prepareStatement(query);
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
     * Saves a list of users to the database.
     */
    public void saveUsers(List<User> users) {
        String query = String.format(
                "INSERT INTO %s.%s (\"UserID\", \"UserName\", \"Email\", \"Password\", \"Blocked\", \"AccessLevel\")" +
                        " VALUES (nextval('habits_model_schema.user_seq'), ?, ?, ?, ?, ?)" +
                        " ON CONFLICT (\"UserID\") DO NOTHING;",
                SCHEMA_NAME, TBL_USERS_NAME);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement pStatement = connection.prepareStatement(query)) {
            for (User user : users) {
                new UserMapper().mapFromObj(pStatement, user);
                pStatement.executeUpdate();
                user.setAccountStatus(EntityStatus.STABLE);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Saves a list of habits to the database.
     */
    public void saveHabits(long userID, List<Habit> habits) {
        String query = String.format(
                "INSERT INTO %s.%s (\"HabitID\", \"UserID\", \"Title\", \"Description\", \"Period\", \"StartDate\")" +
                        " VALUES (nextval('habits_model_schema.habit_seq'), ?, ?, ?, ?, ?)" +
                        " ON CONFLICT (\"HabitID\") DO NOTHING;",
                SCHEMA_NAME, TBL_HABITS_NAME);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement pStatement = connection.prepareStatement(query)) {
            for (Habit habit : habits) {
                new HabitMapper().mapFromObj(pStatement, habit);
                pStatement.executeUpdate();
                saveDates(connection, userID, habit);
                habit.setStatus(EntityStatus.STABLE);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Saves completion dates of specified habit.
     */
    private void saveDates(Connection connection, long userID, Habit habit) {
        String query = String.format(
                "INSERT INTO %s.%s (\"UserID\", \"Title\", \"CompletionDate\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"UserID\", \"Title\", \"CompletionDate\") DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userID);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Updates users with status 'UPDATED'.
     */
    public void updateUsers(List<User> users) {
        String query = String.format(
                "UPDATE %s.%s SET \"UserName\" = ?, \"Email\" = ?, \"Password\" = ?, \"Blocked\" = ?, \"AccessLevel\" = ?" +
                        " WHERE \"UserID\" = ?;",
                SCHEMA_NAME, TBL_USERS_NAME);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement pStatement = connection.prepareStatement(query)) {
            for (User user : users) {
                if (user.getAccountStatus().equals(EntityStatus.UPDATED)) {
                    new UserMapper().mapFromObj(pStatement, user);
                    pStatement.executeUpdate();
                    user.setAccountStatus(EntityStatus.STABLE);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Updates habits with status 'UPDATED' of a specified user.
     */
    public void updateHabits(long userID, List<Habit> habits) {
        String query = String.format(
                "UPDATE %s.%s SET \"UserEmail\" = ?, \"Title\" = ?, \"Description\" = ?, \"Period\" = ?, \"StartDate\" = ?" +
                " WHERE \"HabitID\" = ?;",
                SCHEMA_NAME, TBL_HABITS_NAME);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement pStatement = connection.prepareStatement(query)) {
            for (Habit habit : habits) {
                if (habit.getStatus() != EntityStatus.STABLE) {
                    new HabitMapper().mapFromObj(pStatement, habit);
                    pStatement.setInt(6, habit.getID());
                    pStatement.executeUpdate();
                    updateDates(connection, userID, habit);
                    habit.setStatus(EntityStatus.STABLE);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Updates completion dates for specified habit
     */
    private void updateDates(Connection connection, long userID, Habit habit) {
        String query = String.format(
                "INSERT INTO %s.%s (\"UserEmail\", \"Title\", \"CompletionDate\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"UserEmail\", \"Title\", \"CompletionDate\") DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userID);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Removes users from database whose status field is 'DELETED'
     */
    public void removeUsers(List<User> users) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "DELETE FROM %s.%s WHERE \"UserID\" = ? AND \"Email\" = ?;",
                    SCHEMA_NAME, TBL_USERS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (User user : users) {
                    pStatement.setLong(1, user.getID());
                    pStatement.setString(2, user.getEmail());
                    pStatement.executeUpdate();
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Removes habits from database with status field set to 'DELETED'
     */
    public void removeHabits(long userID, List<Habit> habits) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "DELETE FROM %s.%s WHERE \"HabitID\" = ? AND \"UserEmail\" = ?;",
                    SCHEMA_NAME, TBL_HABITS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (Habit habit : habits) {
                    removeDates(connection, userID, habit);
                    pStatement.setInt(1, habit.getID());
                    pStatement.setLong(2, userID);
                    pStatement.executeUpdate();
                }
            } catch (SQLException e) {
                handleSQLException(e);
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    /**
     * Removes completion dates from database for specified habit
     */
    private void removeDates(Connection connection, long userID, Habit habit) {
        String query = String.format(
                "DELETE FROM %s.%s WHERE \"UserID\" = ? AND \"Title\" = ? AND \"CompletionDate\" = ?;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setLong(1, userID);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }


}

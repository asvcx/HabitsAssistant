package habitsapp.repository;

import habitsapp.models.Habit;
import habitsapp.models.User;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DatabasePostgres implements Database {
    private static final String SCHEMA_NAME = "habits_model_schema";
    private static final String TBL_USERS_NAME = "users";
    private static final String TBL_HABITS_NAME = "habits";
    private static final String TBL_DATES_NAME = "completion_dates";
    private static String dbUrl;
    private static String dbUserName;
    private static String dbPassword;

    public DatabasePostgres(String url, String name, String password) {
        dbUrl = url;
        dbUserName = name;
        dbPassword = password;
    }

    public List<User> loadUsers() {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            Statement statement = connection.createStatement();
            String query = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_USERS_NAME);
            List<User> users = new LinkedList<>();
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while(resultSet.next()) {
                    int id = resultSet.getInt("UserID");
                    String name = resultSet.getString("UserName");
                    String email = resultSet.getString("Email");
                    String password = resultSet.getString("Password");
                    boolean blocked = resultSet.getBoolean("Blocked");
                    User.AccessLevel accessLevel = User.AccessLevel.valueOf(resultSet.getString("AccessLevel"));
                    User user = new User(id, name, email, password, accessLevel, blocked);
                    users.add(user);
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
            return users;
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    public Map<String,Habit> loadHabits() {
        String query = String.format("SELECT * FROM %s.%s;", SCHEMA_NAME, TBL_HABITS_NAME);
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            Statement statement = connection.createStatement();
            Map<String,Habit> habitsByEmail = new HashMap<>();
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while(resultSet.next()) {
                    int id = resultSet.getInt("HabitID");
                    String email = resultSet.getString("UserEmail");
                    String title = resultSet.getString("Title");
                    String description = resultSet.getString("Description");
                    int period = resultSet.getInt("Period");
                    Instant date = resultSet.getTimestamp("StartDate").toInstant();
                    Habit habit = new Habit(id, title, description, period, date);
                    loadDates(email, habit);
                    habitsByEmail.put(email, habit);
                }
                return habitsByEmail;
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }  catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return  new HashMap<>();
    }

    private void loadDates(String userEmail, Habit habit) {
        String query = String.format("SELECT * FROM %s.%s WHERE \"UserEmail\" = ? AND \"Title\" = ?;", SCHEMA_NAME, TBL_DATES_NAME);
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            PreparedStatement pStatement = connection.prepareStatement(query);
            pStatement.setString(1, userEmail);
            pStatement.setString(2, habit.getTitle());
            try (ResultSet resultSet = pStatement.executeQuery()) {
                while(resultSet.next()) {
                    String email = resultSet.getString("UserEmail");
                    String title = resultSet.getString("Title");
                    Instant date = resultSet.getTimestamp("CompletionDate").toInstant();
                    habit.addCompletionDate(date);
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }  catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveUsers(List<User> users) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "INSERT INTO %s.%s (\"UserID\", \"UserName\", \"Email\", \"Password\", \"Blocked\", \"AccessLevel\")" +
                            " VALUES (nextval('habits_model_schema.user_seq'), ?, ?, ?, ?, ?)" +
                            " ON CONFLICT (\"UserID\")" +
                            " DO NOTHING;",
                    SCHEMA_NAME, TBL_USERS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (User user : users) {
                    pStatement.setString(1, user.getName());
                    pStatement.setString(2, user.getEmail());
                    pStatement.setString(3, user.getPassword());
                    pStatement.setBoolean(4, user.isBlocked());
                    pStatement.setObject(5, user.getAccessLevel().name(), Types.OTHER);
                    pStatement.executeUpdate();
                    user.setAccountStatus(User.AccountStatus.STABLE);
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveHabits(String userEmail, List<Habit> habits) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "INSERT INTO %s.%s (\"HabitID\", \"UserEmail\", \"Title\", \"Description\", \"Period\", \"StartDate\")" +
                            " VALUES (nextval('habits_model_schema.habit_seq'), ?, ?, ?, ?, ?)" +
                            " ON CONFLICT (\"HabitID\")" +
                            " DO NOTHING;",
                    SCHEMA_NAME, TBL_HABITS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (Habit habit : habits) {
                    pStatement.setString(1, userEmail);
                    pStatement.setString(2, habit.getTitle());
                    pStatement.setString(3, habit.getDescription());
                    pStatement.setInt(4, habit.getPeriod());
                    pStatement.setObject(5, habit.getStartDate());
                    pStatement.executeUpdate();
                    saveDates(connection, userEmail, habit);
                    habit.resetUpdated();
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveDates(Connection connection, String userEmail, Habit habit) {
        String query = String.format(
                "INSERT INTO %s.%s (\"UserEmail\", \"Title\", \"CompletionDate\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"UserEmail\", \"Title\", \"CompletionDate\")" +
                " DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setString(1, userEmail);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateUsers(List<User> users) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "UPDATE %s.%s SET \"UserName\" = ?, \"Email\" = ?, \"Password\" = ?, \"Blocked\" = ?, \"AccessLevel\" = ?" +
                    " WHERE \"UserID\" = ?;",
                    SCHEMA_NAME, TBL_USERS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (User user : users) {
                    if (user.getAccountStatus().equals(User.AccountStatus.UPDATED)) {
                        pStatement.setString(1, user.getName());
                        pStatement.setString(2, user.getEmail());
                        pStatement.setString(3, user.getPassword());
                        pStatement.setBoolean(4, user.isBlocked());
                        pStatement.setObject(5, user.getAccessLevel().name(), Types.OTHER);
                        pStatement.setInt(6, user.getID());
                        pStatement.executeUpdate();
                        user.setAccountStatus(User.AccountStatus.STABLE);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateHabits(String userEmail, List<Habit> habits) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "UPDATE %s.%s SET \"UserEmail\" = ?, \"Title\" = ?, \"Description\" = ?, \"Period\" = ?, \"StartDate\" = ?" +
                    " WHERE \"HabitID\" = ?;",
                    SCHEMA_NAME, TBL_HABITS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (Habit habit : habits) {
                    if (habit.isUpdated()) {
                        pStatement.setString(1, userEmail);
                        pStatement.setString(2, habit.getTitle());
                        pStatement.setString(3, habit.getDescription());
                        pStatement.setInt(4, habit.getPeriod());
                        pStatement.setObject(5, habit.getStartDate());
                        pStatement.setInt(6, habit.getID());
                        pStatement.executeUpdate();
                        updateDates(connection, userEmail, habit);
                        habit.resetUpdated();
                    }
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDates(Connection connection, String userEmail, Habit habit) {
        String query = String.format(
                "INSERT INTO %s.%s (\"UserEmail\", \"Title\", \"CompletionDate\")" +
                " VALUES (?, ?, ?)" +
                " ON CONFLICT (\"UserEmail\", \"Title\", \"CompletionDate\")" +
                " DO NOTHING;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setString(1, userEmail);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeUsers(List<User> users) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "DELETE FROM %s.%s WHERE \"UserID\" = ? AND \"Email\" = ?;",
                    SCHEMA_NAME, TBL_USERS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (User user : users) {
                    pStatement.setInt(1, user.getID());
                    pStatement.setString(2, user.getEmail());
                    pStatement.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeHabits(String userEmail, List<Habit> habits) {
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            String query = String.format(
                    "DELETE FROM %s.%s WHERE \"HabitID\" = ? AND \"UserEmail\" = ?;",
                    SCHEMA_NAME, TBL_HABITS_NAME);
            try (PreparedStatement pStatement = connection.prepareStatement(query)) {
                for (Habit habit : habits) {
                    removeDates(connection, userEmail, habit);
                    pStatement.setInt(1, habit.getID());
                    pStatement.setString(2, userEmail);
                    pStatement.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeDates(Connection connection, String userEmail, Habit habit) {
        String query = String.format(
                "DELETE FROM %s.%s WHERE \"UserEmail\" = ? AND \"Title\" = ? AND \"CompletionDate\" = ?;",
                SCHEMA_NAME, TBL_DATES_NAME);
        try (PreparedStatement pStatement = connection.prepareStatement(query)) {
            Set<Instant> completionDates = habit.getCompletionDates();
            for (Instant date : completionDates) {
                pStatement.setString(1, userEmail);
                pStatement.setString(2, habit.getTitle());
                pStatement.setObject(3, date);
                pStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


}

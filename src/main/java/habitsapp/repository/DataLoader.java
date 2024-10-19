package habitsapp.repository;

import habitsapp.Main;
import habitsapp.in.InputData;
import habitsapp.in.InputDataByConsole;
import habitsapp.models.Habit;
import habitsapp.models.User;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DataLoader {

    public static InputData inputData;
    public static DatabasePostgres database;
    public static AccountRepository accountRepository;

    public static void load() {
        String dbUrl;
        String dbUserName;
        String dbPassword;
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
            dbUrl = prop.getProperty("db.url");
            dbUserName = prop.getProperty("db.username");
            dbPassword = prop.getProperty("db.password");
        }
        catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        inputData = new InputDataByConsole();
        database = new DatabasePostgres(dbUrl, dbUserName, dbPassword);
        accountRepository = new AccountRepository();

        List<User> users = database.loadUsers();
        for (User user : users) {
            accountRepository.loadUser(user);
        }
        Map<String,Habit> habits = database.loadHabits();
        for (String email : habits.keySet()) {
            accountRepository.loadHabit(email, habits.get(email));
        }
    }

    public static void release() {
        List<User> createdUsers = accountRepository.getUsersList(User.AccountStatus.CREATED);
        database.saveUsers(createdUsers);
        for (User user : createdUsers) {
            List<Habit> habits = new LinkedList<>(accountRepository.getHabitsList(user.getEmail()));
            database.saveHabits(user.getEmail(), habits);
        }

        List<User> updatedUsers = accountRepository.getUsersList(User.AccountStatus.UPDATED);
        for (User user : updatedUsers) {
            List<Habit> habits = new LinkedList<>(accountRepository.getHabitsList(user.getEmail()));
            database.updateHabits(user.getEmail(), habits);
        }
        database.updateUsers(updatedUsers);

        List<User> deletedUsers = accountRepository.getUsersList(User.AccountStatus.DELETED);
        for (User user : deletedUsers) {
            List<Habit> habits = new LinkedList<>(accountRepository.getHabitsList(user.getEmail()));
            database.removeHabits(user.getEmail(), habits);
        }
        database.removeUsers(deletedUsers);
    }

}

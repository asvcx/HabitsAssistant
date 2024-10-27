package habitsapp.server;

import habitsapp.annotations.Measurable;
import habitsapp.client.Main;
import habitsapp.server.repository.Database;
import habitsapp.server.repository.DatabasePostgres;
import habitsapp.server.migration.Migration;
import habitsapp.server.repository.Repository;
import habitsapp.server.service.HabitService;
import habitsapp.server.service.UserService;

import java.io.IOException;
import java.util.Properties;

@Measurable
public class ApplicationContext {
    private final UserService userService;
    private final HabitService habitService;
    private final Repository repository;
    private final Database database;
    private static ApplicationContext instance;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Failed to load PostgreSQL driver: " + e.getMessage());
        }
    }

    private ApplicationContext() {
        Properties dbProp = new Properties();
        try {
            dbProp.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        }
        catch (IOException e) {
            System.out.println("Cannot load properties file");
            throw new RuntimeException(e);
        }
        Migration.migrate(dbProp);
        this.database = new DatabasePostgres(dbProp);
        this.repository = new Repository();
        this.userService = new UserService(repository);
        this.habitService = new HabitService(repository);
    }

    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

    public HabitService getHabitService() {
        return habitService;
    }

    public Repository getRepository() {
        return repository;
    }

    public Database getDatabase() {
        return  database;
    }
}

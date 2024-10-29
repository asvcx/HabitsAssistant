package org.habitsapp.server;

import lombok.Getter;
import org.habitsapp.client.Main;
import org.habitsapp.server.repository.Database;
import org.habitsapp.server.repository.DatabasePostgres;
import org.habitsapp.server.migration.Migration;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.HabitService;
import org.habitsapp.server.service.UserService;

import java.io.IOException;
import java.util.Properties;

@Getter
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

}

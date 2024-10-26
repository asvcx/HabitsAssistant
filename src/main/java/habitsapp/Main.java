package habitsapp;

import habitsapp.data.database.Migration;
import habitsapp.ui.out.MenuConsole;
import habitsapp.data.repository.DataLoader;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class Main {
    /**
     * Invokes migration, data loading, and starts main menu.
     * Before application is closed, DataLoader saves changes to database.
     */
    public static void main(final String[] args) {
        Optional<Properties> dbProperties = readDatabaseProperties();
        if (dbProperties.isEmpty()) {
            return;
        }
        Migration.migrate(dbProperties.get());
        new DataLoader().load();
        new MenuConsole().startGuestMenu();
        new DataLoader().release();
    }
    /**
     * Loads properties file.
     */
    private static Optional<Properties> readDatabaseProperties() {
        Properties dbProperties = new Properties();
        try {
            dbProperties.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        }
        catch (IOException ex) {
            System.out.println("Database connection error");
            return Optional.empty();
        }
        return Optional.of(dbProperties);
    }
}

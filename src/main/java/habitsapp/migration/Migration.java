package habitsapp.migration;

import habitsapp.Main;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Migration {

    private static String dbUrl;
    private static String dbUserName;
    private static String dbPassword;

    public static void migrate() {
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
            dbUrl = prop.getProperty("db.url");
            dbUserName = prop.getProperty("db.username");
            dbPassword = prop.getProperty("db.password");
        }
        catch (IOException ex) {
            System.out.println("Database connection error");
        }

        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            Database db =  DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("db/changelog/changelog.xml", new ClassLoaderResourceAccessor(), db);
            liquibase.update("");
            System.out.println("Migration is completed successfully");
        } catch (SQLException e) {
            System.out.printf("SQLException: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.printf("DatabaseException: " + e.getMessage());
        } catch (LiquibaseException e) {
            System.out.printf("LiquibaseException: " + e.getMessage());
        }
    }

}

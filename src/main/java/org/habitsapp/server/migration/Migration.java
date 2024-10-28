package org.habitsapp.server.migration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Migration {

    public static void migrate(Properties dbProperties) {
        String dbUrl = dbProperties.getProperty("db.url");
        String dbUserName = dbProperties.getProperty("db.username");
        String dbPassword = dbProperties.getProperty("db.password");
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword)) {
            Database db =  DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("db/changelog/changelog.xml", new ClassLoaderResourceAccessor(), db);
            liquibase.update("");
        } catch (SQLException e) {
            System.out.printf("SQLException: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.printf("DatabaseException: " + e.getMessage());
        } catch (LiquibaseException e) {
            System.out.printf("LiquibaseException: " + e.getMessage());
        }
    }

}

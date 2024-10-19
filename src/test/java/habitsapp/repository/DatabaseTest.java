package habitsapp.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseTest {

    private static PostgreSQLContainer<?> postgresContainer;
    private static Connection connection;

    @BeforeAll
    public static void setUp() {
        postgresContainer = new PostgreSQLContainer<>("postgres:latest")
                .withUsername("testPostgres")
                .withPassword("testPassword")
                .withDatabaseName("testDatabaseName");
        postgresContainer.start();
        try {
            connection = DriverManager.getConnection(
                    postgresContainer.getJdbcUrl(),
                    postgresContainer.getUsername(),
                    postgresContainer.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldBeConnectedToDatabase() {
        try {
            assertThat(connection.isValid(3)).isEqualTo(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void tearDown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

}
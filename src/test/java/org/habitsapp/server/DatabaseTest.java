package org.habitsapp.server;

import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.EntityStatus;
import org.habitsapp.model.User;
import org.habitsapp.model.Habit;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.Database;
import org.habitsapp.server.repository.DatabasePostgres;
import org.habitsapp.server.migration.Migration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseTest {

    private static PostgreSQLContainer<?> postgresContainer;
    private static Database database;
    // List of test habits
    static final List<Habit> habits = new LinkedList<>() {{
        add(new Habit(0, "Бег",                 "", 2,  Instant.parse("2020-04-07T06:00:00Z"), 1L));
        add(new Habit(0, "Чтение",              "", 3,  Instant.parse("2020-04-07T10:00:00Z"), 1L));
        add(new Habit(0, "Медитация",           "", 7,  Instant.parse("2020-04-07T13:00:00Z"), 1L));
        add(new Habit(0, "Изучение испанского", "", 2,  Instant.parse("2021-05-12T09:00:00Z"), 2L));
        add(new Habit(0, "Ведение дневника",    "", 1,  Instant.parse("2021-07-15T21:00:00Z"), 2L));
        add(new Habit(0, "Игра на гитаре",      "", 7,  Instant.parse("2022-08-10T17:00:00Z"), 2L));
    }};
    // List of test users
    static final List<User> users = new LinkedList<>() {{
        add(new User(1, "Андрей", "Andrei@mail.ru","",  AccessLevel.USER,  false));
        add(new User(2, "Антон",  "Anton@mail.ru", "",  AccessLevel.USER,  true));
        add(new User(3, "Руслан", "Ruslan@mail.ru","",  AccessLevel.ADMIN, false));
        add(new User(4, "Ольга",  "Olga@mail.ru",  "",  AccessLevel.USER,  false));
        add(new User(5, "Таня",   "Tanya@mail.ru", "",  AccessLevel.USER,  true));
        add(new User(6, "Рудольф","Rudolf@mail.ru","",  AccessLevel.USER,  false));
    }};

    @BeforeEach
    public void setUp() {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withUsername("testPostgres")
                .withPassword("testPassword")
                .withDatabaseName("testDatabaseName");
        postgresContainer.start();

        DatabaseConfig config = new DatabaseConfig();

        config.setUrl(postgresContainer.getJdbcUrl());
        config.setUsername("testPostgres");
        config.setPassword("testPassword");
        config.setSchemaName("habits_model_schema");
        config.setTblUsersName("users");
        config.setTblHabitsName("habits");
        config.setTblDatesName("completion_dates");

        new Migration(config) ;
        database = new DatabasePostgres(config);
    }

    /*
    @Test
    @DisplayName("Should write users and read them back from the database")
    public void shouldWriteUsersAndReadBack() {
        // Given
        List<User> defaultUsers = database.loadUsers();
        int userCount = users.size() + defaultUsers.size();
        // When
        for (User user : users) {
            database.saveUser(user);
        }
        List<User> loadedUsers = database.loadUsers();
        // Then
        assertThat(loadedUsers.size()).isEqualTo(userCount);
    }

    @Test
    @DisplayName("Should write habits and read them back from the database")
    public void shouldWriteHabitsAndReadBack() {
        // Given
        List<Habit> defaultHabits = database.loadHabits().values()
                .stream()
                .flatMap(List::stream)
                .toList();
        int habitCount = habits.size() + defaultHabits.size();

        // When
        for (Habit habit : habits) {
            database.saveHabit(1L, habit);
        }
        List<Habit> loadedHabits = database.loadHabits().values()
                .stream()
                .flatMap(List::stream)
                .toList();
        // Then
        assertThat(loadedHabits.size()).isEqualTo(habitCount);
    }

    @AfterEach
    public void tearDown() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

     */

}
package org.habitsapp.server;

import org.habitsapp.exchange.AdminActionDto;
import org.habitsapp.exchange.PasswordConfirmDto;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.server.controller.*;
import org.habitsapp.server.repository.ProfileAction;
import org.habitsapp.server.security.JwtService;
import org.habitsapp.server.service.HabitServiceImpl;
import org.junit.jupiter.api.*;
import org.habitsapp.exchange.SessionDto;
import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.dto.UserDto;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.AccountRepoImpl;
import org.habitsapp.server.repository.DatabasePostgres;
import org.habitsapp.server.service.UserServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.security.NoSuchAlgorithmException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerTest {
    private static AccountRepoImpl repository;
    private static UserServiceImpl userService;
    private static HabitServiceImpl habitService;
    private static MockMvc mockMvc;

    @BeforeAll
    public static void setup() throws NoSuchAlgorithmException {
        repository = new AccountRepoImpl(new DatabasePostgres(new DatabaseConfig()));
        JwtService jwt = new JwtService();
        userService = new UserServiceImpl(repository, jwt);
        habitService = new HabitServiceImpl(repository, jwt);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new LoginController(userService),
                new LogoutController(userService, repository),
                new ProfileController(userService, repository),
                new HabitController(habitService, repository),
                new AdminController(userService, repository))
                .build();
    }

    @AfterAll
    public static void tearDown() {

    }

    public void registerUser(String name, String email, String password, AccessLevel accessLevel) throws Exception {
        // User tries to register
        UserDto userDto = new UserDto(name, email, password, accessLevel);
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDto);
        // Send request for register profile
        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());
    }

    public MvcResult authorizeUser(String email, String password, AccessLevel accessLevel) throws Exception {
        // Create userDto
        UserDto userDto = new UserDto("", email, password, accessLevel);
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDto);
        // Expected object in response
        SessionDto expectedSession = new SessionDto("", email, accessLevel);
        String expectedSessionJson = objectMapper.writeValueAsString(expectedSession);
        // Send request for login
        return mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedSessionJson))
                .andReturn();
    }

    public void deleteOwnProfile(String token, String password, String userEmail) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String confirmationJson = objectMapper.writeValueAsString(new PasswordConfirmDto(password));
        // Send request for delete profile
        mockMvc.perform(post("/api/profile/delete")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmationJson))
                .andExpect(status().isOk());
    }

    public void logout(String token, ResultMatcher status) throws Exception {
        mockMvc.perform(post("/api/logout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status);
    }

    @Test
    @DisplayName("User authorizes with an existing profile and then logout")
    public void shouldLoginAndLogout() throws Exception {
        // Send request for logout without logged in
        logout("", status().isUnauthorized());

        // Send request for login
        MvcResult loginResult = authorizeUser("admin@mail.ru", "AdminPassword", AccessLevel.ADMIN);

        // Send request for logout
        String token = loginResult.getResponse().getHeader("Authorization");
        logout(token, status().isOk());
    }

    @Test
    @DisplayName("Register user profile then login and delete profile")
    public void shouldRegisterLoginAndDelete() throws Exception {
        // Send request for register
        registerUser("Bishop", "bishop@gmail.com", "123456", AccessLevel.USER);

        // Send request for login
        MvcResult loginResult = authorizeUser("bishop@gmail.com", "123456", AccessLevel.USER);

        // Send request for delete profile
        String token = loginResult.getResponse().getHeader("Authorization");
        deleteOwnProfile(token, "123456", "bishop@gmail.com");
    }

    @Test
    @DisplayName("User registers a profile and Admin removes it")
    public void shouldDeleteProfileByAdmin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String name = "Bishop";
        String email = "bishop@gmail.com";
        String password = "123456";

        // Send request for register profile
        registerUser(name, email, password, AccessLevel.USER);

        // Admin tries to authorize
        MvcResult loginResult = authorizeUser("admin@mail.ru", "AdminPassword", AccessLevel.ADMIN);

        // Admin tries to delete user
        String token = loginResult.getResponse().getHeader("Authorization");
        AdminActionDto actionDto = new AdminActionDto(email, ProfileAction.DELETE);
        String actionJson = objectMapper.writeValueAsString(actionDto);
        mockMvc.perform(post("/api/admin")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actionJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Register, Create habit then delete profile")
    public void shouldManageHabit() throws Exception {
        // Send request for register
        registerUser("Bishop", "bishop@gmail.com", "123456", AccessLevel.USER);

        // Send request for login
        MvcResult loginResult = authorizeUser("bishop@gmail.com", "123456", AccessLevel.USER);
        String token = loginResult.getResponse().getHeader("Authorization");

        // Creating json with HabitDto
        HabitDto habitDto = new HabitDto("title", "description", 1, 0);
        ObjectMapper objectMapper = new ObjectMapper();
        String habitJson = objectMapper.writeValueAsString(habitDto);
        // Send request to create habit
        mockMvc.perform(post("/api/habits")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitJson))
                .andExpect(status().isOk());
        // Send request for delete profile

        deleteOwnProfile(token, "123456", "bishop@gmail.com");
    }

}
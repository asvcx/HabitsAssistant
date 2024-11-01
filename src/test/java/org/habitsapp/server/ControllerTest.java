package org.habitsapp.server;

import org.habitsapp.server.controller.LogoutController;
import org.junit.jupiter.api.*;
import org.habitsapp.exchange.SessionDto;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.server.controller.LoginController;
import org.habitsapp.server.migration.DatabaseConfig;
import org.habitsapp.server.repository.AccountRepository;
import org.habitsapp.server.repository.DatabasePostgres;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerTest {
    AccountRepository repository;
    UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        repository = new AccountRepository(new DatabasePostgres(new DatabaseConfig()));
        userService = new UserService(repository);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new LoginController(userService),
                new LogoutController(userService, repository))
                .build();
    }

    @Test
    @DisplayName("Login with an existing profile and then logout")
    public void shouldLoginAndLogout() throws Exception {
        // Create userDto with credentials
        UserDto userDto = new UserDto("Admin", "admin@mail.ru", "AdminPassword", AccessLevel.ADMIN);
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(userDto);
        // Expected object in response
        SessionDto expectedSession = new SessionDto("Admin", "admin@mail.ru", AccessLevel.ADMIN);
        String expectedSessionJson = objectMapper.writeValueAsString(expectedSession);
        // Send request to logout without logged in
        mockMvc.perform(post("/api/logout")
                        .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isUnauthorized());
        // Send request to login
        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedSessionJson))
                .andReturn();
        String token = loginResult.getResponse().getHeader("Authorization");
        // Send request to logout
        mockMvc.perform(post("/api/logout")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());

    }

}
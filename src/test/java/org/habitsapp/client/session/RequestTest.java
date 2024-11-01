package org.habitsapp.client.session;

import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestTest {

    @Test
    @DisplayName("Try to login with incorrect data, then with correct data, finally logout")
    public void shouldLoginAndThenLogout() {
        Request request = new Request();
        AuthorizationResult result = null;
        // Login with wrong credentials
        result = request.login("WrongEmail", "WrongPassword");
        assertThat(result.isSuccess()).isFalse();
        // Login with correct credentials
        result = request.login("admin@mail.ru", "AdminPassword");
        assertThat(result.isSuccess()).isTrue();
        // Logout
        boolean isLoggedOut = request.logout(result.getToken());
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Register new user, then login, finally delete profile")
    public void shouldRegisterLoginDeleteUser() {
        Request request = new Request();
        RegistrationResult result = null;

        // Register
        result = request.register("Ruslan", "ruslan@mail.ru", "ruslan123");
        assertThat(result.isSuccess()).isTrue();

        // Login
        AuthorizationResult authResult = request.login("ruslan@mail.ru", "ruslan123");
        assertThat(authResult.isSuccess()).isTrue();

        // Delete
        boolean isDeleted = request.deleteOwnProfile(authResult.getToken(), "ruslan123");
        assertThat(isDeleted).isTrue();
    }

    @Test
    @DisplayName("Register new user, then login, finally delete profile")
    public void shouldLoginThenManageHabit() {
        Request request = new Request();
        RegistrationResult result = null;
        // Login
        AuthorizationResult authResult = request.login("admin@mail.ru", "AdminPassword");
        assertThat(authResult.isSuccess()).isTrue();
        // Create habit
        boolean isHabitCreated = request.createHabit(authResult.getToken(), new HabitDto("TestHabit", "", 1, 0));
        assertThat(authResult.isSuccess()).isTrue();
        // Delete habit
        boolean isHabitDeleted = request.deleteHabit(authResult.getToken(), new HabitDto("TestHabit", "", 1, 0));
        assertThat(authResult.isSuccess()).isTrue();
    }

}

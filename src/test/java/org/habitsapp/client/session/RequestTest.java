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
        assertThat(result.success()).isFalse();
        // Login with correct credentials
        result = request.login("admin@mail.ru", "AdminPassword");
        assertThat(result.success()).isTrue();
        // Logout
        boolean isLoggedOut = request.logout(result.token());
        assertThat(isLoggedOut).isTrue();
    }

    @Test
    @DisplayName("Register new user, then login, finally delete profile")
    public void shouldRegisterLoginDeleteUser() {
        Request request = new Request();

        // Register
        RegistrationResult regResult = request.register("Ruslan", "ruslan@mail.ru", "ruslan123");
        assertThat(regResult.success()).isTrue();

        // Login
        AuthorizationResult authResult = request.login("ruslan@mail.ru", "ruslan123");
        assertThat(authResult.success()).isTrue();

        // Delete
        boolean isDeleted = request.deleteOwnProfile(authResult.token(), "ruslan123");
        assertThat(isDeleted).isTrue();
    }

    @Test
    @DisplayName("Authorize, create habit, finally delete habit")
    public void shouldLoginThenManageHabit() {
        Request request = new Request();
        // Login
        AuthorizationResult authResult = request.login("admin@mail.ru", "AdminPassword");
        assertThat(authResult.success()).isTrue();

        // Create habit
        boolean isHabitCreated = request.createHabit(authResult.token(), new HabitDto("TestHabit", "", 1, 0));
        assertThat(isHabitCreated).isTrue();
        // Delete habit

        boolean isHabitDeleted = request.deleteHabit(authResult.token(), new HabitDto("TestHabit", "", 1, 0));
        assertThat(isHabitDeleted).isTrue();
    }

}

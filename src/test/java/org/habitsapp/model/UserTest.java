package org.habitsapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    private final User user = new User("Name", "name@mail.ru", "UserPass");

    @Test
    @DisplayName("Should not pass when user is different")
    void shouldNotPassWhenUserIsDifferent() {
        // Given
        User clonedUser = user.clone();
        assertThat(user.isUserEquivalent(clonedUser)).isTrue();
        // When
        clonedUser.setPassword("NewPass");
        // Then
        assertThat(user.isUserEquivalent(clonedUser)).isFalse();
    }

}

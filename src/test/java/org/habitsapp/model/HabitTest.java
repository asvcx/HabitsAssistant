package org.habitsapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitTest {

    Habit habit = new Habit("test", "test", 1);

    @Test
    @DisplayName("Should return correct completion percentage")
    void shouldReturnCorrectCompletionPercent() {
        // Given
        assertThat(habit.getCompletionPercent()).isEqualTo(0);
        // When
        habit.markAsCompleted();
        // Then
        assertThat(habit.getCompletionPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should increase current streak after marking as completed")
    void shouldIncreaseStreakAfterMarking() {
        // Given
        assertThat(habit.getCurrentStreak()).isEqualTo(0);
        // When
        habit.markAsCompleted();
        // Then
        assertThat(habit.getCurrentStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return true on first marking, false on subsequent marks")
    void shouldReturnTrueWithFirstMarking() {
        // When
        assertThat(habit.markAsCompleted()).isTrue();
        // Then
        assertThat(habit.markAsCompleted()).isFalse();
    }

}

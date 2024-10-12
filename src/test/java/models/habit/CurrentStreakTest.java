package models.habit;

import habitsapp.models.Habit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrentStreakTest {

    Habit habit = new Habit("test", "test", 1);

    @Test
    void shouldIncreaseStreakAfterMarking() {
        assertThat(habit.getCurrentStreak()).isEqualTo(0);
        habit.markAsCompleted();
        assertThat(habit.getCurrentStreak()).isEqualTo(1);
    }

}

package habitsapp.data.models;

import habitsapp.data.models.Habit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitTest {

    Habit habit = new Habit("test", "test", 1);

    @Test
    void shouldReturnCorrectCompletionPercent() {
        assertThat(habit.getCompletionPercent()).isEqualTo(0);
        habit.markAsCompleted();
        assertThat(habit.getCompletionPercent()).isEqualTo(100);
    }

    @Test
    void shouldIncreaseStreakAfterMarking() {
        assertThat(habit.getCurrentStreak()).isEqualTo(0);
        habit.markAsCompleted();
        assertThat(habit.getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void shouldReturnTrueWithFirstMarking() {
        assertThat(habit.markAsCompleted()).isEqualTo(true);
        assertThat(habit.markAsCompleted()).isEqualTo(false);
    }

}

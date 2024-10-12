package models.habit;

import habitsapp.models.Habit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MarkingAsCompletedTest {

    Habit habit = new Habit("test", "test", 1);

    @Test
    void shouldReturnFalseAfterSecondMarking() {
        assertThat(habit.markAsCompleted()).isEqualTo(true);
        assertThat(habit.markAsCompleted()).isEqualTo(false);
    }

}

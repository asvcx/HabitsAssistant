package models.habit;

import habitsapp.models.Habit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompletionPercentRangeTest {

    Habit habit = new Habit("test", "test", 1);

    @Test
    void shouldReturnZeroBeforeMarking() {
        assertThat(habit.getCompletionPercent()).isEqualTo(0);
    }

    @Test
    void shouldReturnHundredAfterMarking() {
        habit.markAsCompleted();
        assertThat(habit.getCompletionPercent()).isEqualTo(100);
    }

}

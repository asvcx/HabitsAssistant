package org.habitsapp.client.out;

import org.habitsapp.client.out.HabitFilter;
import org.habitsapp.models.Habit;
import org.habitsapp.client.session.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

public class HabitFilterTest {

    static final Set<Habit> habits = new TreeSet<>() {{
        add(new Habit(1, "Бег",                 "", 2,  Instant.parse("2020-04-07T06:00:00Z"), 1L));
        add(new Habit(2, "Чтение",              "", 3,  Instant.parse("2020-04-07T10:00:00Z"), 1L));
        add(new Habit(3, "Медитация",           "", 7,  Instant.parse("2020-04-07T13:00:00Z"), 1L));
        add(new Habit(4, "Изучение испанского", "", 2,  Instant.parse("2021-05-12T09:00:00Z"), 1L));
        add(new Habit(5, "Ведение дневника",    "", 1,  Instant.parse("2021-07-15T21:00:00Z"), 1L));
        add(new Habit(6, "Игра на гитаре",      "", 7,  Instant.parse("2022-08-10T17:00:00Z"), 1L));
    }};

    @BeforeAll
    static void init() {
        Session.setHabits(habits);
    }

    @Test
    @DisplayName("Should filter habits by title")
    void shouldFilterHabitsByName() {
        assertThat(HabitFilter.byName("Шитье").count()).isEqualTo(0);
        assertThat(HabitFilter.byName("Альпинизм").count()).isEqualTo(0);
        assertThat(HabitFilter.byName("Бег").count()).isEqualTo(1);
        assertThat(HabitFilter.byName("Чтение").count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should filter habits by period")
    void shouldFilterHabitsByPeriod() {
        assertThat(HabitFilter.byPeriod(1).count()).isEqualTo(1);
        assertThat(HabitFilter.byPeriod(2).count()).isEqualTo(2);
        assertThat(HabitFilter.byPeriod(3).count()).isEqualTo(1);
        assertThat(HabitFilter.byPeriod(4).count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should filter habits correctly by date of creation")
    void shouldFilterHabitsByDateCreation() {
        // Given
        Instant dayNoHabitsAdded = Instant.parse("2020-04-05T12:00:00Z");
        Instant dayOneHabitAdded = Instant.parse("2021-05-12T12:00:00Z");
        Instant dayThreeHabitsAdded = Instant.parse("2020-04-07T12:00:00Z");

        // When
        long dayNoHabits = HabitFilter.byDateCreation(dayNoHabitsAdded).count();
        long dayOneHabit = HabitFilter.byDateCreation(dayOneHabitAdded).count();
        long dayThreeHabits = HabitFilter.byDateCreation(dayThreeHabitsAdded).count();

        // Then
        assertThat(dayNoHabits).isEqualTo(0);
        assertThat(dayOneHabit).isEqualTo(1);
        assertThat(dayThreeHabits).isEqualTo(3);
    }

    @Test
    @DisplayName("Should filter habits by completion percent")
    void shouldFilterHabitsByCompletionPercent() {
        assertThat(HabitFilter.byCompletionPercent(0, 0).count()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should filter habits by current streak")
    void shouldFilterHabitsByCurrentStreak() {
        assertThat(HabitFilter.byCurrentStreak(0, 0).count()).isEqualTo(6);
    }

}

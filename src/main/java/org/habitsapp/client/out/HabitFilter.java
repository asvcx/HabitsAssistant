package org.habitsapp.client.out;

import org.habitsapp.client.session.Session;
import org.habitsapp.models.Habit;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

public class HabitFilter {

    public static Stream<String> byName(String titleSubStr) {
        return Session.getHabits().stream()
                    .filter(h -> h.getTitle().contains(titleSubStr))
                    .map(Habit::toString);
    }

    public static Stream<String> byPeriod(int period) {
        return Session.getHabits().stream()
                    .filter(h -> h.getPeriod() == period)
                    .map(Habit::toString);
    }

    public static Stream<String> byDateCreation(Instant dateTime) {
        return Session.getHabits().stream()
                    .filter(h -> {
                        long hoursDifference = Duration.between(h.getStartDate(), dateTime).toHours();
                        return Math.abs(hoursDifference) < 13;
                    })
                    .map(v -> v + "[дата создания : " + v.getStartDate() + "]");
    }

    public static Stream<String> byCompletionPercent(int minPercent, int maxPercent) {
        return Session.getHabits().stream()
                    .filter(h -> h.getCompletionPercent() >= minPercent && h.getCompletionPercent() <= maxPercent)
                    .map(v -> v + "[процент выполнения : " + v.getCompletionPercent() + " %]");
    }

    public static Stream<String> byCurrentStreak(int minStreak, int maxStreak) {
        return Session.getHabits().stream()
                    .filter(h -> h.getCurrentStreak() >= minStreak && h.getCurrentStreak() <= maxStreak)
                    .map(v -> v + "[серия выполнения : " + v.getCurrentStreak() + "]");
    }

}

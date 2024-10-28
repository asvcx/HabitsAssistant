package org.habitsapp.server.service;

import org.habitsapp.client.session.HabitCreationResult;
import org.habitsapp.models.EntityStatus;
import org.habitsapp.models.Habit;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.User;
import org.habitsapp.server.repository.Repository;

import java.util.Optional;
import java.util.Set;

public class HabitService {
    private final Repository repository;

    public HabitService(Repository repository) {
        this.repository = repository;
    }

    public HabitCreationResult createHabit(String email, String token, HabitDto newHabit) {
        if (!repository.checkToken(email, token)) {
            return new HabitCreationResult(false, "You are not logged in");
        }
        Optional<Habit> habitOpt = repository.getHabitByTitle(email,newHabit.getTitle());
        if (habitOpt.isPresent()) {
            return new HabitCreationResult(false, "Habit with specified title already exists");
        }
        Habit habit = new Habit(newHabit.getTitle(), newHabit.getDescription(), newHabit.getPeriod());
        repository.createHabit(email, habit);
        return new HabitCreationResult(true, "Habit successfully created");
    }

    public boolean markHabitAsCompleted(String email, String token, String habitTitle) {
        if (!repository.checkToken(email, token)) {
            return false;
        }
        User user = repository.getUserByEmail(email.toLowerCase()).get();
        Set<Habit> habits = repository.getHabitsOfUser(email);
        if (habits.isEmpty()) {
            return false;
        }
        Optional<Habit> habit = repository.getHabitByTitle(email, habitTitle);
        if (habit.isEmpty() || habit.get().getStatus() == EntityStatus.DELETED) {
            return false;
        }
        habit.get().markAsCompleted();
        habit.get().setStatus(EntityStatus.UPDATED);
        return true;
    }

    public boolean editHabit(String email, String token, Habit oldHabit, Habit newHabit) {
        if (!repository.checkToken(email, token)) {
            return false;
        }
        Set<Habit> userHabits = repository.getHabitsOfUser(email);
        if (!userHabits.contains(oldHabit)) {
            return false;
        }
        userHabits.remove(oldHabit);
        newHabit.setStatus(EntityStatus.UPDATED);
        userHabits.add(newHabit);
        return true;
    }

    public boolean deleteHabit(String email, String token, String title) {
        if (!repository.checkToken(email, token)) {
            return false;
        }
        Set<Habit> userHabits = repository.getHabitsOfUser(email);
        Optional<Habit> habitOpt = userHabits.stream()
                .filter(h -> h.getTitle().equals(title))
                .findFirst();
        habitOpt.ifPresent(h -> h.setStatus(EntityStatus.DELETED));
        return habitOpt.isPresent();
    }

}

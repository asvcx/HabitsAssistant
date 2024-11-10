package org.habitsapp.server.service;

import org.example.HabitService;
import org.habitsapp.model.Habit;
import org.habitsapp.server.repository.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class HabitServiceImpl implements HabitService {
    private final AccountRepo repository;

    @Autowired
    public HabitServiceImpl(AccountRepo repository) {
        this.repository = repository;
    }

    public boolean createHabit(Long userId, String title, String description, int period) {
        Optional<Habit> habitOpt = repository.getHabitByTitle(userId, title);
        if (habitOpt.isPresent()) {
            return false;
        }
        Habit habit = new Habit(title, description, period);
        if (repository.createHabit(userId, habit)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean markHabitAsCompleted(Long userId, String title) {
        Optional<Map<String,Habit>> habits = repository.getHabitsOfUser(userId);
        if (habits.isEmpty()) {
            return false;
        }
        Optional<Habit> habit = repository.getHabitByTitle(userId, title);
        if (habit.isEmpty()) {
            return false;
        }
        habit.get().markAsCompleted();
        repository.markHabit(userId, habit.get());
        return true;
    }

    public boolean editHabit(Long userId, String oldTitle, String title, String description, int period) {
        Optional<Map<String,Habit>> userHabits = repository.getHabitsOfUser(userId);
        if (userHabits.isEmpty() || !userHabits.get().containsKey(oldTitle)) {
            return false;
        }
        return repository.updateHabit(userId, oldTitle, title, description, period);
    }

    public boolean deleteHabit(Long userId, String title) {
        return repository.deleteHabit(userId, title);
    }

}

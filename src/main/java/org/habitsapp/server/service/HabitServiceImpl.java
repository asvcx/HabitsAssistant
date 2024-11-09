package org.habitsapp.server.service;

import io.jsonwebtoken.Claims;
import org.habitsapp.model.result.HabitCreationResult;
import org.habitsapp.model.Habit;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class HabitServiceImpl implements HabitService {
    private final AccountRepo repository;
    private final JwtService jwt;

    @Autowired
    public HabitServiceImpl(AccountRepo repository, JwtService jwt) {
        this.repository = repository;
        this.jwt = jwt;
    }

    public HabitCreationResult createHabit(Long userId, String token, HabitDto habitDto) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return new HabitCreationResult(false, "You are not logged in");
        }
        Optional<Habit> habitOpt = repository.getHabitByTitle(userId, habitDto.getTitle());
        if (habitOpt.isPresent()) {
            return new HabitCreationResult(false, "Habit with specified title already exists");
        }
        Habit habit = new Habit(habitDto.getTitle(), habitDto.getDescription(), habitDto.getPeriod());
        if (repository.createHabit(userId, habit)) {
            return new HabitCreationResult(true, "Habit successfully created");
        } else {
            return new HabitCreationResult(false, "Failed to create a habit");
        }
    }

    public boolean markHabitAsCompleted(Long userId, String token, String habitTitle) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        Optional<Map<String,Habit>> habits = repository.getHabitsOfUser(userId);
        if (habits.isEmpty()) {
            return false;
        }
        Optional<Habit> habit = repository.getHabitByTitle(userId, habitTitle);
        if (habit.isEmpty()) {
            return false;
        }
        habit.get().markAsCompleted();
        repository.markHabit(userId, habit.get());
        return true;
    }

    public boolean editHabit(Long userId, String token, Habit oldHabit, Habit newHabit) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        Optional<Map<String,Habit>> userHabits = repository.getHabitsOfUser(userId);
        if (userHabits.isEmpty() || !userHabits.get().containsKey(oldHabit.getTitle())) {
            return false;
        }
        return repository.updateHabit(userId, oldHabit, newHabit);
    }

    public boolean deleteHabit(Long userId, String token, String title) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        return repository.deleteHabit(userId, title);
    }

}

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

    public HabitCreationResult createHabit(String email, String token, HabitDto habitDto) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return new HabitCreationResult(false, "You are not logged in");
        }
        Optional<Habit> habitOpt = repository.getHabitByTitle(email, habitDto.getTitle());
        if (habitOpt.isPresent()) {
            return new HabitCreationResult(false, "Habit with specified title already exists");
        }
        Habit habit = new Habit(habitDto.getTitle(), habitDto.getDescription(), habitDto.getPeriod());
        if (repository.createHabit(email, habit)) {
            return new HabitCreationResult(true, "Habit successfully created");
        } else {
            return new HabitCreationResult(false, "Failed to create a habit");
        }
    }

    public boolean markHabitAsCompleted(String email, String token, String habitTitle) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        Optional<Map<String,Habit>> habits = repository.getHabitsOfUser(email);
        if (habits.isEmpty()) {
            return false;
        }
        Optional<Habit> habit = repository.getHabitByTitle(email, habitTitle);
        if (habit.isEmpty()) {
            return false;
        }
        habit.get().markAsCompleted();
        repository.markHabit(email.toLowerCase(), habit.get());
        return true;
    }

    public boolean editHabit(String email, String token, Habit oldHabit, Habit newHabit) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        Optional<Map<String,Habit>> userHabits = repository.getHabitsOfUser(email);
        if (userHabits.isEmpty() || !userHabits.get().containsKey(oldHabit.getTitle())) {
            return false;
        }
        return repository.updateHabit(email, oldHabit, newHabit);
    }

    public boolean deleteHabit(String email, String token, String title) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null) {
            return false;
        }
        return repository.deleteHabit(email, title);
    }

}

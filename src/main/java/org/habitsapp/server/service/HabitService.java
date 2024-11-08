package org.habitsapp.server.service;

import org.habitsapp.model.Habit;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.model.result.HabitCreationResult;

public interface HabitService {
    HabitCreationResult createHabit(String email, String token, HabitDto habitDto);
    boolean markHabitAsCompleted(String email, String token, String habitTitle);
    boolean editHabit(String email, String token, Habit oldHabit, Habit newHabit);
    boolean deleteHabit(String email, String token, String title);
}

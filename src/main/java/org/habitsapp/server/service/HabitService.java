package org.habitsapp.server.service;

import org.habitsapp.model.Habit;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.model.result.HabitCreationResult;

public interface HabitService {
    HabitCreationResult createHabit(Long userId, String token, HabitDto habitDto);
    boolean markHabitAsCompleted(Long userId, String token, String habitTitle);
    boolean editHabit(Long userId, String token, Habit oldHabit, Habit newHabit);
    boolean deleteHabit(Long userId, String token, String title);
}

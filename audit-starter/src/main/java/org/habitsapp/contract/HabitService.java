package org.habitsapp.contract;

public interface HabitService {
    boolean createHabit(Long userId, String title, String description, int period);
    boolean markHabitAsCompleted(Long userId, String habitTitle);
    boolean editHabit(Long userId, String oldTitle, String title, String description, int period);
    boolean deleteHabit(Long userId, String title);
}

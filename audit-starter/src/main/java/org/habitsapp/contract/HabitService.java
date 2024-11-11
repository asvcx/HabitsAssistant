package org.habitsapp.contract;

public interface HabitService {
    abstract boolean createHabit(Long userId, String title, String description, int period);
    abstract boolean markHabitAsCompleted(Long userId, String habitTitle);
    abstract boolean editHabit(Long userId, String oldTitle, String title, String description, int period);
    abstract boolean deleteHabit(Long userId, String title);
}

package org.habitsapp.exchange;

import org.habitsapp.models.dto.HabitDto;

public class HabitChangeDto {
    private HabitDto oldHabit;
    private HabitDto newHabit;

    public HabitChangeDto() {}

    public HabitChangeDto(HabitDto oldHabit, HabitDto newHabit) {
        this.oldHabit = oldHabit;
        this.newHabit = newHabit;
    }

    public HabitDto getOldHabit() {
        return oldHabit;
    }

    public void setOldHabit(HabitDto oldHabit) {
        this.oldHabit = oldHabit;
    }

    public HabitDto getNewHabit() {
        return newHabit;
    }

    public void setNewHabit(HabitDto newHabit) {
        this.newHabit = newHabit;
    }
}

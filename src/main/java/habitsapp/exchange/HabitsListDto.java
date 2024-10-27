package habitsapp.exchange;

import habitsapp.models.dto.HabitDto;

import java.util.List;

public class HabitsListDto {
    private List<HabitDto> habits;

    public HabitsListDto() {}

    public HabitsListDto(List<HabitDto> habits) {
        this.habits = habits;
    }

    public void setHabits(List<HabitDto> habits) {
        this.habits = habits;
    }

    public List<HabitDto> getHabits() {
        return habits;
    }
}

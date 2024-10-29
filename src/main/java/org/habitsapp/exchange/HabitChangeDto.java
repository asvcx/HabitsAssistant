package org.habitsapp.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.habitsapp.models.dto.HabitDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HabitChangeDto {
    private HabitDto oldHabit;
    private HabitDto newHabit;
}

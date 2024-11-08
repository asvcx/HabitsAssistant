package org.habitsapp.exchange;

import lombok.*;
import org.habitsapp.model.dto.HabitDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitChangeDto {
    private HabitDto oldHabit;
    private HabitDto newHabit;
}

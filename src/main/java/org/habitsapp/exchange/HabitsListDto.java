package org.habitsapp.exchange;

import lombok.*;
import org.habitsapp.model.dto.HabitDto;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitsListDto {
    private List<HabitDto> habits;
}

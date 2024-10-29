package org.habitsapp.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.habitsapp.models.dto.HabitDto;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HabitsListDto {
    private List<HabitDto> habits;
}

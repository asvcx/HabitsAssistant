package habitsapp.models.dto;

import habitsapp.models.Habit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HabitMapper {
    HabitMapper INSTANCE = Mappers.getMapper(HabitMapper.class);
    HabitDto habitToHabitDto(Habit habit);
    Habit habitDtoToHabit(HabitDto userDTO);
}

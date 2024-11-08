package org.habitsapp.model.dto;

import org.habitsapp.model.Habit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface HabitMapper {
    HabitMapper INSTANCE = Mappers.getMapper(HabitMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "period", target = "period")
    @Mapping(source = "startedDate", target = "startedDate")
    @Mapping(source = "completionDates", target = "completionDates")
    @Mapping(source = "userId", target = "userId")
    HabitDto habitToHabitDto(Habit habit);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "period", target = "period")
    @Mapping(source = "startedDate", target = "startedDate")
    @Mapping(source = "completionDates", target = "completionDates")
    @Mapping(source = "userId", target = "userId")
    Habit habitDtoToHabit(HabitDto userDTO);
}

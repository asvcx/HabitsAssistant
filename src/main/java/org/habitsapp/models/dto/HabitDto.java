package org.habitsapp.models.dto;

import lombok.Data;

import java.time.Instant;
import java.util.TreeSet;

@Data
public class HabitDto implements Cloneable {
    private int id;
    private String title;
    private String description;
    private int period;
    private Instant startedDate;
    private TreeSet<Instant> completionDates;
    private long userId;

    public HabitDto() {}

    public HabitDto(String title, String description, int period, long userId) {
        this.title = title;
        this.description = description;
        this.period = period;
        this.userId = userId;
    }

    @Override
    public HabitDto clone() {
        try {
            return (HabitDto) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}

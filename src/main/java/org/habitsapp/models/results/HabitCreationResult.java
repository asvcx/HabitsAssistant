package org.habitsapp.models.results;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HabitCreationResult {
    private boolean success;
    private String message;
}

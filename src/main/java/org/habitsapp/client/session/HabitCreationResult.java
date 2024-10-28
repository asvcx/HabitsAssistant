package org.habitsapp.client.session;

public class HabitCreationResult {
    private boolean success;
    private String message;

    public HabitCreationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

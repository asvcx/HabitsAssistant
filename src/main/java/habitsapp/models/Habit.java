package habitsapp.models;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.time.Instant;
import java.util.TreeSet;

public class Habit implements Comparable<Habit>, Cloneable {

    private int id;
    private String title;
    private String description;
    private int period;
    private Instant startedDate;
    private TreeSet<Instant> completionDates;
    private boolean updated;

    public Habit() {
        completionDates = new TreeSet<>();
        startedDate = Instant.now();
        title = "";
        description = "";
        period = 1;
        updated = false;
    }

    public Habit(String title, String description, int period) {
        this();
        setTitle(title);
        setDescription(description);
        setPeriod(period);
    }

    public Habit(int id, String title, String description, int period, Instant date) {
        this(title, description, period);
        this.id = id;
        this.startedDate = date;
    }

    @Override
    public int compareTo(Habit other) {
        return this.title.compareTo(other.title);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Habit habit = (Habit) obj;
        return title.equals(habit.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s [период: %d, описание: %s]", this.title, this.period, this.description);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public void setTitle(String title) {
        if (title != null && !Objects.equals(title, "")) {
            this.title = title;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeriod(int period) {
        this.period = Math.max(period, 1);
    }

    public boolean isUpdated() {
        return this.updated;
    }

    public void setUpdated() {
        this.updated = true;
    }

    public void resetUpdated() {
        this.updated = false;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public int getPeriod() {
        return this.period;
    }

    public Instant getStartDate() {
        return this.startedDate;
    }

    public TreeSet<Instant> getCompletionDates() {
        return new TreeSet<>(this.completionDates);
    }

    public int getCompletionPercent() {
        Instant currentDate = Instant.now();
        int daysAfterCreate = (int) ChronoUnit.DAYS.between(startedDate, currentDate);
        int completedCount = completionDates.size();
        int maxCount = 1 + daysAfterCreate / period;
        return (completedCount * 100) / maxCount;
    }

    public int getCurrentStreak() {
        if (completionDates.isEmpty()) {
            return 0;
        }
        Instant currentDate = Instant.now();
        int streak = 0;
        for (Instant completionDate : completionDates.descendingSet()) {
            long daysBetween = ChronoUnit.DAYS.between(completionDate, currentDate);
            if (daysBetween <= period) {
                streak++;
                currentDate = currentDate.minus(period, ChronoUnit.DAYS);
            } else {
                break;
            }
        }
        return streak;
    }

    public boolean markAsCompleted() {
        Instant now = Instant.now();
        if (completionDates.isEmpty()) {
            completionDates.add(now);
            return true;
        }
        Instant lastCompletionDate = completionDates.last();
        long daysSinceLastCompletion = ChronoUnit.DAYS.between(lastCompletionDate, now);
        if (daysSinceLastCompletion == period) {
            completionDates.add(now);
            return true;
        } else {
            return false;
        }
    }

    public boolean addCompletionDate(Instant date) {
        completionDates.add(date);
        return true;
    }

    @Override
    public Habit clone() {
        try {
            Habit cloned = (Habit) super.clone();
            cloned.completionDates = new TreeSet<>(this.completionDates);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}

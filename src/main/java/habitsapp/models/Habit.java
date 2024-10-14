package habitsapp.models;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.time.Instant;
import java.util.TreeSet;

public class Habit implements Comparable<Habit>, Cloneable {

    private String title;
    private String description;
    private int period;
    private final Instant startedDate;
    private TreeSet<Instant> completionDate;

    public Habit() {
        completionDate = new TreeSet<>();
        startedDate = Instant.now();
        this.title = "";
        this.description = "";
        this.period = 1;
    }

    public Habit(String title, String description, int period) {
        this();
        setTitle(title);
        setDescription(description);
        setPeriod(period);
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

    public boolean setTitle(String title) {
        if (!Objects.equals(title, "")) {
            this.title = title;
            return true;
        }
        return false;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeriod(int period) {
        this.period = Math.max(period, 1);
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

    public Instant getStartedDate() {
        return this.startedDate;
    }

    public TreeSet<Instant> getCompletionDate() {
        return new TreeSet<>(this.completionDate);
    }

    public int getCompletionPercent() {
        Instant currentDate = Instant.now();
        int daysAfterCreate = (int) ChronoUnit.DAYS.between(startedDate, currentDate);
        int completedCount = completionDate.size();
        int maxCount = 1 + daysAfterCreate / period;
        return (completedCount * 100) / maxCount;
    }

    public int getCurrentStreak() {
        if (completionDate.isEmpty()) {
            return 0;
        }
        Instant currentDate = Instant.now();
        int streak = 0;
        for (Instant completionDate : completionDate.descendingSet()) {
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
        if (completionDate.isEmpty()) {
            completionDate.add(now);
            return true;
        }
        Instant lastCompletionDate = completionDate.last();
        long daysSinceLastCompletion = ChronoUnit.DAYS.between(lastCompletionDate, now);
        if (daysSinceLastCompletion == period) {
            completionDate.add(now);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Habit clone() {
        try {
            Habit cloned = (Habit) super.clone();
            cloned.completionDate = new TreeSet<>(this.completionDate);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}

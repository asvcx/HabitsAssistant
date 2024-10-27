package habitsapp.models.dto;

import java.time.Instant;
import java.util.TreeSet;

public class HabitDto {
    private int id;
    private String title;
    private String description;
    private int period;
    private Instant startedDate;
    private TreeSet<Instant> completionDates;
    private long userID;

    public HabitDto() {}

    public HabitDto(String title, String description, int period, long userID) {
        this.title = title;
        this.description = description;
        this.period = period;
        this.userID = userID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }


    public Instant getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Instant startedDate) {
        this.startedDate = startedDate;
    }

    public TreeSet<Instant> getCompletionDates() {
        return completionDates;
    }

    public void setCompletionDates(TreeSet<Instant> completionDates) {
        this.completionDates = completionDates;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
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

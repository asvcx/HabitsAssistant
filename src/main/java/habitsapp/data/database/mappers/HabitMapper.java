package habitsapp.data.database.mappers;

import habitsapp.data.models.Habit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

public class HabitMapper implements ResultSetMapper<Habit> {

    public Habit mapToObj(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("HabitID");
        long userID = resultSet.getLong("UserID");
        String title = resultSet.getString("Title");
        String description = resultSet.getString("Description");
        int period = resultSet.getInt("Period");
        Instant date = resultSet.getTimestamp("StartDate").toInstant();

        return new Habit(id, title, description, period, date, userID);
    }

    public void mapFromObj(PreparedStatement pStatement, Habit habit) throws SQLException {
        pStatement.setLong(1, habit.getUserID());
        pStatement.setString(2, habit.getTitle());
        pStatement.setString(3, habit.getDescription());
        pStatement.setInt(4, habit.getPeriod());
        pStatement.setObject(5, habit.getStartDate(), Types.OTHER);
    }

}
package org.habitsapp.server.repository.dbmappers;

import org.habitsapp.models.Habit;

import java.sql.*;
import java.time.Instant;

public class DBHabitMapper implements ResultSetMapper<Habit> {

    public Habit mapToObj(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("habit_id");
        long userID = resultSet.getLong("user_id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        int period = resultSet.getInt("period");
        Instant date = resultSet.getTimestamp("start_date").toInstant();

        return new Habit(id, title, description, period, date, userID);
    }

    public void mapFromObj(PreparedStatement pStatement, Habit habit) throws SQLException {
        pStatement.setLong(1, habit.getUserId());
        pStatement.setString(2, habit.getTitle());
        pStatement.setString(3, habit.getDescription());
        pStatement.setInt(4, habit.getPeriod());
        pStatement.setTimestamp(5, Timestamp.from(habit.getStartDate()));
    }

}
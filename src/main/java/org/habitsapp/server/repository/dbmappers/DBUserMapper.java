package org.habitsapp.server.repository.dbmappers;

import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DBUserMapper implements ResultSetMapper<User> {

    public User mapToObj(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("UserID");
        String name = resultSet.getString("UserName");
        String email = resultSet.getString("Email");
        String password = resultSet.getString("Password");
        boolean blocked = resultSet.getBoolean("Blocked");
        AccessLevel accessLevel = AccessLevel.valueOf(resultSet.getString("AccessLevel"));
        return new User(id, name, email, password, accessLevel, blocked);
    }

    public void mapFromObj(PreparedStatement preparedStatement, User user) throws SQLException {
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getEmail());
        preparedStatement.setString(3, user.getPassword());
        preparedStatement.setBoolean(4, user.isBlocked());
        preparedStatement.setObject(5, user.getAccessLevel().name(), Types.OTHER);
    }
}
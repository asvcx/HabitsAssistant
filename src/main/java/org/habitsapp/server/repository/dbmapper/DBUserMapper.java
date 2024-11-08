package org.habitsapp.server.repository.dbmapper;

import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DBUserMapper implements ResultSetMapper<User> {

    public User mapToObj(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("user_id");
        String name = resultSet.getString("user_name");
        String email = resultSet.getString("email");
        String password = resultSet.getString("password");
        boolean blocked = resultSet.getBoolean("blocked");
        AccessLevel accessLevel = AccessLevel.valueOf(resultSet.getString("access_level"));
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
package habitsapp.server.repository.dbmappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper<T> {
    T mapToObj(ResultSet resultSet) throws SQLException;
    void mapFromObj(PreparedStatement pStatement, T obj) throws SQLException;
}
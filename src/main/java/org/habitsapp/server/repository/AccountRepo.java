package org.habitsapp.server.repository;

import org.habitsapp.model.Habit;
import org.habitsapp.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface AccountRepo {
    List<User> getUsers();
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserById(long id);
    Optional<Map<String,Habit>> getHabitsOfUser(String email);
    Optional<Habit> getHabitByTitle(String email, String title);

    boolean isUserExists(String email);
    boolean isUserExists(long id);

    boolean createUser(User user);
    boolean updateUser(String email, String token, User changedUser);
    boolean updateUser(String email, Consumer<User> userAction);
    boolean deleteUser(String email, String token);

    boolean createHabit(String email, Habit habit);
    boolean updateHabit(String email, Habit oldHabit, Habit newHabit);
    boolean markHabit(String email, Habit habit);
    boolean deleteHabit(String email, String title);

//    boolean isTokenExists(String token);
//    boolean isUserAuthorized(String token);
//    Optional<User> getUserByToken(String token);
//    void addToken(String token, User user);
//    boolean removeToken(String token);
//    boolean checkToken(String email, String token);
//    boolean cancelToken(String token);

    boolean checkPassword(String email, String password);


}

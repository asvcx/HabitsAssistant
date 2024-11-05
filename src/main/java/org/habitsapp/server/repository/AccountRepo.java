package org.habitsapp.server.repository;

import org.habitsapp.models.Habit;
import org.habitsapp.models.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface AccountRepo {
    boolean isUserExists(String email);
    boolean isUserExists(long id);
    boolean isUserAuthorized(String token);
    boolean loadUser(User user);
    boolean createUser(User user);
    boolean replaceUser(String email, String token, User changedUser);
    boolean cancelToken(String token);
    boolean deleteUser(String email, String token);
    boolean updateUser(String email, Consumer<User> userAction);
    boolean loadHabit(String email, Habit habit);
    boolean changeHabitProperties(String email, Habit oldHabit, Habit newHabit);
    boolean createHabit(String email, Habit habit);
    boolean setHabits(long userID, List<Habit> habitsList);
    boolean deleteHabit(String email, String title);
    Optional<User> getUserByEmail(String email);
    boolean isTokenExists(String token);
    Optional<User> getUserByToken(String token);
    void addToken(String token, User user);
    boolean removeToken(String token);
    boolean checkToken(String email, String token);
    boolean checkPassword(String email, String password);
    Optional<Map<String,Habit>> getHabitsOfUser(String email);
    Optional<Habit> getHabitByTitle(String email, String title);
    List<User> getUsers();
}

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
    Optional<Map<String,Habit>> getHabitsOfUser(Long id);
    Optional<Habit> getHabitByTitle(Long id, String title);

    boolean isUserExists(String email);
    boolean isUserExists(long id);

    boolean createUser(User user);
    boolean updateUser(Long id, String token, User changedUser);
    boolean updateUser(Long id, Consumer<User> userAction);
    boolean deleteUser(Long id);

    boolean createHabit(Long id, Habit habit);
    boolean updateHabit(Long id, String oldTitle, String title, String description, int period);
    boolean markHabit(Long id, Habit habit);
    boolean deleteHabit(Long id, String title);
    boolean checkPassword(Long id, String password);


}

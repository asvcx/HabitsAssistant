package org.habitsapp.server.repository;

import org.habitsapp.models.EntityStatus;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;

import java.util.*;
import java.util.stream.Collectors;

public class Repository {
    public enum ProfileAction {
        BLOCK,
        UNBLOCK,
        DELETE
    }
    private static final Map<User,TreeSet<Habit>> habitsOfUser = new HashMap<>();
    private static final Map<String,User> userByEmail = new HashMap<>();
    private static final Map<Long,User> userByID = new HashMap<>();
    private static final Map<String,User> userByToken = new HashMap<>();

    public Repository() {

    }

    public boolean isUserExists(String email) {
        if (!userByEmail.containsKey(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean isUserExists(long id) {
        if (!userByID.containsKey(id)) {
            return false;
        }
        User user = userByID.get(id);
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean isUserAuthorized(String token) {
        if (!userByToken.containsKey(token)) {
            return false;
        }
        User user = userByToken.get(token);
        return user.getAccountStatus() != EntityStatus.DELETED;
    }

    public boolean loadUser(User user) {
        if (!isUserExists(user.getEmail().toLowerCase())) {
            habitsOfUser.put(user, new TreeSet<>());
            userByEmail.put(user.getEmail().toLowerCase(), user);
            userByID.put(user.getID(), user);
            return true;
        }
        return false;
    }

    public boolean createUser(User user) {
        if (loadUser(user)) {
            user.setAccountStatus(EntityStatus.CREATED);
            return true;
        }
        return false;
    }

    public boolean replaceUser(String email, String token, User changedUser) {
        if (!isUserExists(email.toLowerCase())) {
            System.out.println("User not exists");
            return false;
        }
        User user = userByEmail.get(email);
        String newEmail = changedUser.getEmail().toLowerCase();
        Set<Habit> userHabits = getHabitsOfUser(email);
        habitsOfUser.remove(user);
        userByEmail.remove(email);
        userByToken.remove(token);
        userByEmail.put(newEmail, changedUser);
        habitsOfUser.put(changedUser, (TreeSet<Habit>) userHabits);
        userByToken.put(token, user);
        setHabits(changedUser.getID(), userHabits.stream().toList());
        user.setAccountStatus(EntityStatus.UPDATED);
        return true;
    }

    public boolean deleteUser(String email, String token) {
        if (!isUserExists(email)) {
            return false;
        }
        User user = userByEmail.get(email);
        userByToken.remove(token);
        user.setAccountStatus(EntityStatus.DELETED);
        return true;
    }

    public boolean loadHabit(String email, Habit habit) {
        if (!isUserExists(email.toLowerCase())) {
            return false;
        }
        User user = userByEmail.get(email.toLowerCase());
        if (!habitsOfUser.containsKey(user)) {
            return false;
        }
        TreeSet<Habit> habitsSet = habitsOfUser.get(user);
        habitsSet.add(habit);
        return true;
    }

    public boolean createHabit(String email, Habit habit) {
        email = email.toLowerCase();
        if (loadHabit(email, habit)) {
            habit.setStatus(EntityStatus.CREATED);
            habit.setUserID(userByEmail.get(email).getID());
            return true;
        }
        return false;
    }

    public boolean setHabits(long userID, List<Habit> habits) {
        if (!isUserExists(userID)) {
            return false;
        }
        User user = userByID.get(userID);
        if (!habitsOfUser.containsKey(user)) {
            habitsOfUser.remove(user);
        }
        TreeSet<Habit> habitsSet = new TreeSet<>(habits);
        habitsOfUser.put(user, habitsSet);
        return true;
    }

    public Optional<User> getUserByEmail(String email) {
        if (!isUserExists(email.toLowerCase())) {
            return Optional.empty();
        }
        return Optional.of(userByEmail.get(email.toLowerCase()));
    }

    public boolean isTokenExists(String token) {
        return userByToken.containsKey(token);
    }

    public Optional<User> getUserByToken(String token) {
        if (userByToken.containsKey(token)) {
            return Optional.of(userByToken.get(token));
        }
        return Optional.empty();
    }

    public void addToken(String token, User user) {
        userByToken.put(token, user);
    }

    public boolean removeToken(String token) {
        if (userByToken.containsKey(token)) {
            userByToken.remove(token);
            return true;
        }
        return false;
    }

    public boolean checkToken(String email, String token) {
        Optional<User> eUser = getUserByEmail(email);
        Optional<User> tUser = getUserByToken(token);
        return eUser.isPresent() && tUser.isPresent() && email.equals(tUser.get().getEmail());
    }

    public boolean checkPassword(String email, String password) {
        Optional<User> user = getUserByEmail(email);
        return user.isPresent() && password.equals(user.get().getPassword());
    }

    public Set<Habit> getHabitsOfUser(String email) {
        if (!isUserExists(email.toLowerCase())) {
            return new TreeSet<>();
        }
        User user = userByEmail.get(email.toLowerCase());
        if (habitsOfUser.containsKey(user)) {
            return habitsOfUser.get(user).stream()
                    .filter(h -> h.getStatus() != EntityStatus.DELETED)
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        return new TreeSet<>();
    }

    public List<User> getUsersByStatus(EntityStatus accountStatus) {
        List<User> users = new LinkedList<>();
        for (User user: habitsOfUser.keySet()) {
            if (user.getAccountStatus().equals(accountStatus)) {
                users.add(user);
            }
        }
        return users;
    }

    public Optional<Habit> getHabitByTitle(String email, String title) {
        Set<Habit> habits = getHabitsOfUser(email.toLowerCase());
        if (habits.isEmpty()) {
            return Optional.empty();
        }
        return habits.stream()
                .filter(h -> title.equals(h.getTitle()))
                .findFirst();
    }

    public List<User> getUsers() {
        return new LinkedList<>(habitsOfUser.keySet());
    }


}
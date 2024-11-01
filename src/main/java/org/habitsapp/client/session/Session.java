package org.habitsapp.client.session;

import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.models.dto.UserMapper;

import java.util.Set;
import java.util.TreeSet;

public class Session {

    private static UserDto currentProfile = null;
    private static Set<Habit> currentHabits = new TreeSet<>();
    private static String token;

    public static void start(UserDto user, String token) {
        if (user != null) {
            setProfile(user);
            Session.token = token;
            Request request = new Request();
            Set<Habit> habits = request.getHabits(getToken());
            setHabits(habits);
        }
    }

    public static void start(User user, String token) {
        if (user != null) {
            UserDto userDto = UserMapper.INSTANCE.userToUserDto(user);
            start(userDto, token);
        }
    }

    public static void update() {
        Request request = new Request();
        Set<Habit> habits = request.getHabits(getToken());
        setHabits(habits);
    }

    public static void setProfile(UserDto user) {
        currentProfile = user;
    }

    public static void setProfile(User user) {
        currentProfile = UserMapper.INSTANCE.userToUserDto(user);
    }

    public static Set<Habit> getHabits() {
        return currentHabits;
    }

    public static void setHabits(Set<Habit> habits) {
        currentHabits = habits;
    }

    public static UserDto getProfile() {
        return currentProfile;
    }

    public static long getID() {
        return currentProfile.getId();
    }

    public static String getEmail() {
        return currentProfile.getEmail();
    }

    public static String getName() {
        return currentProfile.getName();
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Session.token = token;
    }

    public static boolean isAuthorized() {
        return currentProfile != null;
    }

    public static boolean isAdmin() {
        return (currentProfile != null) && (currentProfile.getAccessLevel() == AccessLevel.ADMIN);
    }

    public static void logout() {
        Request request = new Request();
        request.logout(token);
        currentProfile = null;
        currentHabits = new TreeSet<>();
        System.out.println("Вы вышли из профиля.");
    }


}

package org.habitsapp.model;

public class UserValidator {

    public static boolean isNameValid(String name) {
        return name.length() > 3;
    }

    public static boolean isDateValid(String dateString) {
        return dateString.matches("^\\d{1,2}-\\d{1,2}-\\d{2}$");
    }

    public static boolean isEmailValid(String email) {
        return email.matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]{2,})+$");
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 5;
    }
}

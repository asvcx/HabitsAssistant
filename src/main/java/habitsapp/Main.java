package habitsapp;

import habitsapp.console.Menu;
import habitsapp.data.DataController;
import habitsapp.models.User;

public class Main {

    public static void main(String[] args) {
        User testUser = new User("user", "user@mail.ru", "userPsw");
        User testAdmin = new User("admin", "admin@mail.ru", "adminPsw");
        testAdmin.setAccessLevel(User.AccessLevel.ADMIN);
        DataController.addUser(testUser);
        DataController.addUser(testAdmin);

        Menu.startGuestMenu();
    }

}

package habitsapp;

import habitsapp.console.UIService;
import habitsapp.data.DataController;
import habitsapp.models.User;

public class Main {

    public static void main(String[] args) {
        User user = new User();
        user.setName("test");
        user.setEmail("test@mail.ru");
        user.setPassword("121212");
        DataController.addUser(user);

        UIService.startGuest();
    }

}

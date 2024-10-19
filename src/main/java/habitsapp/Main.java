package habitsapp;

import habitsapp.migration.Migration;
import habitsapp.out.MenuConsole;
import habitsapp.repository.DataLoader;

public class Main {

    public static void main(String[] args) {
        Migration.migrate();
        DataLoader.load();
        String testAccountsMsg = """
         ______________________________________________________________________________________
         Test accounts:
            1. Standard user. Name: "user"; Email: "user@mail.ru"; Password: "UserPassword".
            2. Administrator. Name: "admin"; Email: "admin@mail.ru"; Password: "AdminPassword".
         ______________________________________________________________________________________
         """;
        System.out.println(testAccountsMsg);
        MenuConsole menuConsole = new MenuConsole();
        menuConsole.startGuestMenu();
        DataLoader.release();
    }

}

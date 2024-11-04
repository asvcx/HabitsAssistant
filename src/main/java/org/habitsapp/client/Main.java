package org.habitsapp.client;

import org.habitsapp.client.out.MenuConsole;

public class Main {
    /**
     * Starts main menu
     */
    public static void main(final String[] args) {
        MenuConsole menuConsole = new MenuConsole();
        menuConsole.startGuestMenu();
    }
}

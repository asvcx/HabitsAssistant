package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.habitsapp.server.repository.AccountRepo;

public class TokenReader {

    public static String readToken(HttpServletRequest req, AccountRepo repo) {
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        return token.substring(7);
    }

}

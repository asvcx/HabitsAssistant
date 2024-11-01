package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.habitsapp.server.repository.AccountRepository;

public class TokenReader {

    public static String readToken(HttpServletRequest req, AccountRepository repo) {
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Token ")) {
            return null;
        }
        String tokenValue = token.substring(6);
        if (!repo.isUserAuthorized(tokenValue)) {
            return null;
        }
        return tokenValue;
    }

}

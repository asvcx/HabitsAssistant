package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.server.repository.AccountRepository;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/logout")
public class LogoutController {

    private final UserService userService;
    private final AccountRepository repository;

    @Autowired
    public LogoutController(UserService userService, AccountRepository repository) {
        this.userService = userService;
        this.repository = repository;
    }

    /**
     * Logout from profile
     */
    @PostMapping
    protected ResponseEntity<MessageDto> logout(HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDto("You have not been authorized"));
        }
        // Try to logout
        if (userService.logoutUser(token)) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Successfully logged out"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDto("You have not been authorized"));
        }
    }

}

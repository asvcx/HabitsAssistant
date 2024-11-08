package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/logout")
@RequiredArgsConstructor
public class LogoutController {

    private final UserService userService;
    private final AccountRepo repository;

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
        if (userService.logoutUser(token)) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Successfully logged out"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageDto("You have not been authorized"));
        }
    }

}

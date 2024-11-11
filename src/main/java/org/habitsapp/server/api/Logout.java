package org.habitsapp.server.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.contract.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/logout")
@RequiredArgsConstructor
public class Logout {

    private final UserService userService;

    /**
     * Logout from profile
     */
    @PostMapping
    protected ResponseEntity<MessageDto> logout(HttpServletRequest req) {
        long id = Long.parseLong((String)req.getAttribute("id"));
        if (userService.logoutUser(id)) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Successfully logged out"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageDto("You have not been authorized"));
        }
    }

}

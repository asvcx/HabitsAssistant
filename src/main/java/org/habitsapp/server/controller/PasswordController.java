package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.exchange.PasswordChangeDto;
import org.habitsapp.model.User;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Optional;

@RestController
@RequestMapping("api/password")
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;
    private final AccountRepo repository;

    @PutMapping
    protected ResponseEntity<MessageDto> change(@RequestBody PasswordChangeDto pswChange, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty() || pswChange == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to change user password
        long id = (long) req.getAttribute("id");
        Optional<User> user = repository.getUserById(id);
        boolean isChanged = user.isPresent() && userService.editUserPassword(
                pswChange.getUserEmail(), token, pswChange.getOldPassword(), pswChange.getNewPassword()
        );
        if (isChanged) {
            return ResponseEntity.ok().body(new MessageDto("Password changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto("Failed to change password"));
        }
    }

}

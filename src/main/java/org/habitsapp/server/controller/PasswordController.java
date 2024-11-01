package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.exchange.PasswordChangeDto;
import org.habitsapp.models.User;
import org.habitsapp.server.repository.AccountRepository;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Optional;

@RestController
@RequestMapping("api/password")
public class PasswordController {

    private final UserService userService;
    private final AccountRepository repository;

    @Autowired
    public PasswordController(UserService userService, AccountRepository accountRepository) {
        this.userService = userService;
        this.repository = accountRepository;
    }

    @PutMapping
    protected ResponseEntity<MessageDto> changePassword(@RequestBody PasswordChangeDto pswChange, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty() || pswChange == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to change user password
        Optional<User> user = repository.getUserByToken(token);
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

package org.habitsapp.server.controller;

import org.habitsapp.exchange.MessageDto;
import org.habitsapp.exchange.PasswordConfirmation;
import org.habitsapp.exchange.ProfileChangeDto;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.models.results.RegistrationResult;
import org.habitsapp.server.repository.AccountRepository;
import org.habitsapp.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final AccountRepository repository;

    @Autowired
    public ProfileController(UserService userService, AccountRepository repository) {
        this.userService = userService;
        this.repository = repository;
    }

    /**
     *  Create user profile
     */
    @PostMapping
    public ResponseEntity<MessageDto> createUserProfile(@RequestBody UserDto userDto, HttpServletRequest req) {
        // Check dto
        if (userDto == null || userDto.getEmail() == null
                || userDto.getPassword() == null || userDto.getName() == null) {
            return ResponseEntity.badRequest().body(new MessageDto("User data have not been provided"));
        }
        // Try to create user profile
        RegistrationResult result = userService.registerUser(userDto);
        if (result.isSuccess()) {
            return ResponseEntity.ok().body(new MessageDto(result.getMessage()));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto(result.getMessage()));
        }
    }

    /**
     *  Change user profile
     */
    @PutMapping
    public ResponseEntity<MessageDto> changeUserProfile(@RequestBody ProfileChangeDto usrChange, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDto("You have not been authorized"));
        }
        if (usrChange == null || usrChange.getOldEmail() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDto("Cannot get new value for field"));
        }
        // Try to change user profile
        Optional<User> user = repository.getUserByToken(token);
        boolean isChanged = user.isPresent() && userService.editUserData(
                usrChange.getOldEmail(), token, usrChange.getNewEmail(), usrChange.getNewName()
        );
        if (isChanged) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Profile changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageDto("Failed to change profile"));
        }
    }

    /**
     *  Delete user profile
     */
    @PostMapping("/delete")
    public ResponseEntity<MessageDto> deleteUserProfile(@RequestBody PasswordConfirmation confirmation, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        String password = confirmation.getPassword();
        if (token == null || token.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageDto("You have not been authorized"));
        }
        // Try to delete user profile
        Optional<User> user = repository.getUserByToken(token);
        boolean isDeleted = user.isPresent() && userService.deleteUser(user.get().getEmail(), token, user.get().getPassword());
        if (isDeleted) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Profile deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageDto("Failed to delete profile"));
        }
    }

}
package org.habitsapp.server.api;

import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.exchange.PasswordConfirmDto;
import org.habitsapp.exchange.ProfileChangeDto;
import org.habitsapp.model.User;
import org.habitsapp.model.dto.UserDto;
import org.habitsapp.model.result.RegistrationResult;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class Profile {

    private final UserService userService;
    private final AccountRepo repository;

    /**
     *  Create user profile
     */
    @PostMapping("/create")
    public ResponseEntity<MessageDto> create(@RequestBody UserDto userDto, HttpServletRequest req) {
        // Check dto
        if (userDto == null || userDto.getEmail() == null
                || userDto.getPassword() == null || userDto.getName() == null) {
            return ResponseEntity.badRequest().body(new MessageDto("User data have not been provided"));
        }
        // Try to create user profile
        RegistrationResult result = userService.registerUser(userDto);
        if (result.success()) {
            return ResponseEntity.ok().body(new MessageDto(result.message()));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto(result.message()));
        }
    }

    /**
     *  Change user profile
     */
    @PutMapping
    public ResponseEntity<MessageDto> change(@RequestBody ProfileChangeDto usrChange, HttpServletRequest req) {
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
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        boolean isChanged = user.isPresent() && userService.editUserData(
                id, token, usrChange.getNewEmail(), usrChange.getNewName()
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
    public ResponseEntity<MessageDto> delete(@RequestBody PasswordConfirmDto confirmation, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        String password = confirmation.getPassword();
        if (token == null || token.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageDto("You have not been authorized"));
        }
        // Try to delete user profile
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        boolean isDeleted = user.isPresent() && userService.deleteUser(id, token, user.get().getPassword());
        if (isDeleted) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Profile deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageDto("Failed to delete profile"));
        }
    }

}
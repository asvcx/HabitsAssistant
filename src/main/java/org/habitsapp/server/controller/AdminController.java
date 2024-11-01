package org.habitsapp.server.controller;

import org.habitsapp.exchange.AdminActionDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.User;
import org.habitsapp.server.repository.AccountRepository;
import org.habitsapp.server.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final AccountRepository repository;

    @Autowired
    public AdminController(UserService userService, @Qualifier("accountRepository") AccountRepository repository) {
        this.userService = userService;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<String>> getUsersInfo(HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        Optional<User> admin = repository.getUserByToken(token);

        if (token == null || token.isEmpty() || admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> usersInfo = userService.getUsersInfo(admin.get().getEmail(), token);
        if (admin.get().getAccessLevel() == AccessLevel.ADMIN) {
            return ResponseEntity.ok(usersInfo);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(List.of("Failed to delete user"));
        }
    }

    @PostMapping
    public ResponseEntity<MessageDto> manageUserProfile(@RequestBody AdminActionDto actionDto, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);

        if (token == null || token.isEmpty() || actionDto == null || actionDto.getProfileAction() == null || actionDto.getEmailToManage() == null) {
            return ResponseEntity.badRequest().body(new MessageDto("Bad request"));
        }

        Optional<User> admin = repository.getUserByToken(token);
        Optional<User> user = repository.getUserByEmail(actionDto.getEmailToManage());

        boolean isManaged = admin.isPresent() && user.isPresent()
                && userService.manageUserProfile(admin.get().getEmail(), token, actionDto.getEmailToManage(), actionDto.getProfileAction());

        if (isManaged) {
            return ResponseEntity.ok(new MessageDto("Action performed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto("Failed to perform user action"));
        }
    }
}
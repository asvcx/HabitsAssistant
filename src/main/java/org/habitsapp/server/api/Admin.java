package org.habitsapp.server.api;

import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.AdminActionDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.User;
import org.habitsapp.server.repository.AccountRepo;
import org.example.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class Admin {

    private final UserService userService;
    private final AccountRepo repository;

    @GetMapping
    public ResponseEntity<List<String>> getUsersInfo(HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> admin = repository.getUserById(id);

        if (token == null || token.isEmpty() || admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> usersInfo = userService.getUsersInfo(admin.get().getId(), token);
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

        if (token == null || token.isEmpty() || actionDto == null || actionDto.getProfileAction() == null
                || actionDto.getEmailToManage() == null) {
            return ResponseEntity.badRequest().body(new MessageDto("Bad request"));
        }

        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> admin = repository.getUserById(id);
        Optional<User> user = repository.getUserByEmail(actionDto.getEmailToManage());

        boolean isManaged = admin.isPresent() && user.isPresent()
                && userService.manageUserProfile(admin.get().getId(), token, actionDto.getEmailToManage(),
                actionDto.getProfileAction().getDeclaringClass().getName());

        if (isManaged) {
            return ResponseEntity.ok(new MessageDto("Action performed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto("Failed to perform user action"));
        }
    }
}
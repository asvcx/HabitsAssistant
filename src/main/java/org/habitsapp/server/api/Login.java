package org.habitsapp.server.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.model.dto.UserDto;
import org.example.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class Login {
    private final UserService userService;

    /**
     * Login to profile
     */
    @PostMapping
    protected ResponseEntity<MessageDto> login(@RequestBody UserDto userDto, HttpServletRequest req) {
        if (userDto == null || userDto.getEmail() == null || userDto.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String token = userService.authorizeUser(userDto.getEmail(), userDto.getPassword());
        if (!token.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            return ResponseEntity.ok().headers(headers).body(new MessageDto("You have successfully logged in"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}

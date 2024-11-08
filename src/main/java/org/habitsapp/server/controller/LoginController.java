package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.SessionDto;
import org.habitsapp.model.dto.UserDto;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.server.service.UserService;
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
public class LoginController {
    private final UserService userService;

    /**
     * Login to profile
     */
    @PostMapping
    protected ResponseEntity<SessionDto> login(@RequestBody UserDto userDto, HttpServletRequest req) {
        if (userDto == null || userDto.getEmail() == null || userDto.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        AuthorizationResult result = userService.authorizeUser(userDto.getEmail(), userDto.getPassword());
        if (result.success()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + result.token());
            SessionDto sessionDto = new SessionDto(userDto.getName(), userDto.getEmail(), userDto.getAccessLevel());
            return ResponseEntity.ok().headers(headers).body(sessionDto);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}

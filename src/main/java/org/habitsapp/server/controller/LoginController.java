package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.habitsapp.exchange.SessionDto;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.server.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Login to profile
     */
    @PostMapping
    protected ResponseEntity<SessionDto> login(@RequestBody UserDto userDto, HttpServletRequest req) {
        if (userDto == null || userDto.getEmail() == null || userDto.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        AuthorizationResult result = userService.authorizeUser(userDto.getEmail(), userDto.getPassword());
        if (result.isSuccess()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + result.getToken());
            SessionDto sessionDto = new SessionDto(userDto.getName(), userDto.getEmail(), userDto.getAccessLevel());
            return ResponseEntity.ok().headers(headers).body(sessionDto);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}

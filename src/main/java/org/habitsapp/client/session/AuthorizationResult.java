package org.habitsapp.client.session;

import org.habitsapp.models.dto.UserDto;

public class AuthorizationResult {
    private boolean success;
    private String message;
    private UserDto userDTO;
    private String token;

    public AuthorizationResult() {
    }

    public AuthorizationResult(boolean success, String message, String token, UserDto userDTO) {
        this.success = success;
        this.message = message;
        this.userDTO = userDTO;
        this.token = token;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDto getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDto userDTO) {
        this.userDTO = userDTO;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
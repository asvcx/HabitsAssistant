package org.habitsapp.model.result;

import org.habitsapp.model.dto.UserDto;

public record AuthorizationResult(boolean success, String message, String token, UserDto userDto) {}
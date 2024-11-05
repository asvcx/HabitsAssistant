package org.habitsapp.models.results;

import org.habitsapp.models.dto.UserDto;

public record AuthorizationResult(boolean success, String message, String token, UserDto userDto) {}
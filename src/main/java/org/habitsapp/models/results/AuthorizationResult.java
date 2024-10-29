package org.habitsapp.models.results;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.habitsapp.models.dto.UserDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationResult {
    private boolean success;
    private String message;
    private String token;
    private UserDto userDTO;
}
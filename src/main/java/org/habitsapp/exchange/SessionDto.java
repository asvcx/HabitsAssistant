package org.habitsapp.exchange;

import lombok.*;
import org.habitsapp.models.AccessLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String userName;
    private String userEmail;
    private AccessLevel accessLevel;
}

package org.habitsapp.exchange;

import lombok.*;
import org.habitsapp.models.AccessLevel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String userName;
    private String userEmail;
    private AccessLevel accessLevel;
}

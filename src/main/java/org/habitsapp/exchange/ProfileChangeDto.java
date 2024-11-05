package org.habitsapp.exchange;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChangeDto {
    private String oldEmail;
    private String newEmail;
    private String newName;
}

package org.habitsapp.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChangeDto {
    private String oldEmail;
    private String newEmail;
    private String newName;
}

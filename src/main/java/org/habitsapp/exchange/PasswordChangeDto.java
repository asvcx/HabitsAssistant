package org.habitsapp.exchange;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {
    private String userEmail;
    private String oldPassword;
    private String newPassword;
}

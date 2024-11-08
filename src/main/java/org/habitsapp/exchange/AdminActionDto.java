package org.habitsapp.exchange;

import lombok.*;
import org.habitsapp.server.repository.ProfileAction;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionDto {
    private String emailToManage;
    private ProfileAction profileAction;
}

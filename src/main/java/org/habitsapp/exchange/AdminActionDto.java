package org.habitsapp.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.habitsapp.server.repository.Repository;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionDto {
    private String emailToManage;
    private Repository.ProfileAction profileAction;
}
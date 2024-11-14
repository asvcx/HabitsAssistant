package org.habitsapp.model.dto;

import lombok.Data;
import org.habitsapp.model.AccessLevel;

@Data
public class UserDto implements Cloneable {
    private Long id;
    private String name;
    private String email;
    private String password;
    private AccessLevel accessLevel;

    public UserDto() {
        accessLevel = AccessLevel.USER;
    }

    public UserDto(Long id, String email, String password) {
        this();
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public UserDto(Long id, String name, String email, String password, AccessLevel accessLevel) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.accessLevel = accessLevel;
    }

    @Override
    public UserDto clone() {
        try {
            return (UserDto) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

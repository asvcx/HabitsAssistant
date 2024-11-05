package org.habitsapp.models.dto;

import lombok.Data;
import org.habitsapp.models.AccessLevel;

@Data
public class UserDto implements Cloneable {
    private long id;
    private String name;
    private String password;
    private String email;
    private AccessLevel accessLevel;

    public UserDto() {
        accessLevel = AccessLevel.USER;
    }

    public UserDto(long id, String email, String password) {
        this();
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public UserDto(String name, String email, String password, AccessLevel accessLevel) {
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

package org.habitsapp.models.dto;

import org.habitsapp.models.AccessLevel;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public AccessLevel getAccessLevel() {
        return this.accessLevel;
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

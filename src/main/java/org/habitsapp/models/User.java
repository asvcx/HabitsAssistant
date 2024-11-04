package org.habitsapp.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User implements Cloneable {
    private long id;
    private String name;
    private String email;
    private String password;
    private AccessLevel accessLevel;
    private boolean blocked;
    private EntityStatus accountStatus;

    public User() {
        blocked = false;
        accessLevel = AccessLevel.USER;
        accountStatus = EntityStatus.STABLE;
    }

    public User(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(long id, String name, String email, String password, AccessLevel accessLevel, boolean blocked) {
        this(name, email, password);
        this.id = id;
        this.accessLevel = accessLevel;
        this.blocked = blocked;
    }

    @Override
    public String toString() {
        return String.format("%s, [доступ: %s] [заблокированный: %b]", this.email, this.accessLevel, this.blocked);
    }

    public void block() {
        blocked = true;
    }

    public void unblock() {
        blocked = false;
    }

    public boolean isAdmin() {
        return (accessLevel == AccessLevel.ADMIN);
    }

    public boolean comparePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isUserEquivalent(User user) {
        return (this.email.equals(user.email)
                && this.password.equals(user.password)
                && this.name.equals(user.name)
                && this.accessLevel.equals(user.accessLevel)
                && (this.isAdmin() == user.isAdmin())
        );
    }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

package habitsapp.models;

public class User implements Cloneable {

    public enum AccessLevel {
        USER,
        ADMIN
    }

    private String name;
    private String email;
    private String password;
    private AccessLevel accessLevel;
    private boolean blocked;

    public User() {
        setAccessLevel(AccessLevel.USER);
        blocked = false;
    }

    public User(String name, String email, String password) {
        this();
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    @Override
    public String toString() {
        return String.format("%s, [доступ: %s] [заблокированный: %b]", this.email, this.accessLevel, this.blocked);
    }

    private String encodePsw(String psw) {
        return psw;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public void block() {
        blocked = true;
    }

    public void unblock() {
        blocked = false;
    }

    public void setPassword(String password) {
        this.password = encodePsw(password);
    }

    public void setAccessLevel(AccessLevel acl) {
        this.accessLevel = acl;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public boolean isAdmin() {
        return (accessLevel == AccessLevel.ADMIN);
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public boolean isPasswordProper(String psw) {
        return this.password.equals(encodePsw(psw));
    }

    public boolean isUserAuthentic(User user) {
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

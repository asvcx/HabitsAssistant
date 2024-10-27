package habitsapp.models;

public class User implements Cloneable {
    private long id;
    private String name;
    private String email;
    private String password;
    private boolean blocked;
    private AccessLevel accessLevel;
    private EntityStatus accountStatus;

    public User() {
        blocked = false;
        accessLevel = AccessLevel.USER;
        accountStatus = EntityStatus.STABLE;
    }

    public User(String name, String email, String password) {
        this();
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    public User(long id, String name, String email, String password, AccessLevel accessLevel, boolean blocked) {
        this(name, email, password);
        this.id = id;
        this.accessLevel = accessLevel;
        this.blocked = blocked;
    }

    public void setID(long id) {
        this.id = id;
    }

    public long getID() {
        return this.id;
    }

    @Override
    public String toString() {
        return String.format("%s, [доступ: %s] [заблокированный: %b]", this.email, this.accessLevel, this.blocked);
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
        this.password = password;
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

    public AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isAdmin() {
        return (accessLevel == AccessLevel.ADMIN);
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void setAccountStatus(EntityStatus status) {
        this.accountStatus = status;
    }

    public EntityStatus getAccountStatus() {
        return this.accountStatus;
    }

    public boolean comparePassword(String password) {
        return this.password.equals(password);
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

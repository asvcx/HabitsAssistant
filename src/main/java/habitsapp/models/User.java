package habitsapp.models;

public class User implements Cloneable {

    private String name;
    private String email;
    private String password;

    public User() {

    }

    public User(String name, String email, String password) {
        setName(name);
        setEmail(email);
        setPassword(password);
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

    public void setPassword(String password) {
        this.password = encodePsw(password);
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    private String getPassword() {
        return this.password;
    }

    public void resetPassword() {

    }

    public boolean isPasswordValid(String psw) {
        return this.password.equals(encodePsw(psw));
    }

    @Override
    public User clone() {
        try {
            User clone = (User) super.clone();
            clone.setPassword("");
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

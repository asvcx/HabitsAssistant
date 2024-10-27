package habitsapp.exchange;

public class PasswordChangeDto {
    private String userEmail;
    private String oldPassword;
    private String newPassword;

    public PasswordChangeDto() {}

    public PasswordChangeDto(String userEmail, String oldPassword, String newPassword) {
        this.userEmail = userEmail;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

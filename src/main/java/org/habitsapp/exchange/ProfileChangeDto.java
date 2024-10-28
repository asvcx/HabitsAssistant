package org.habitsapp.exchange;

public class ProfileChangeDto {
    private String oldEmail;
    private String newEmail;
    private String newName;

    public ProfileChangeDto() {}

    public ProfileChangeDto(String oldEmail, String newEmail, String newName) {
        this.oldEmail = newEmail;
        this.newEmail = newEmail;
        this.newName = newName;
    }

    public String getOldEmail() {
        return oldEmail;
    }

    public void setOldEmail(String oldEmail) {
        this.oldEmail = oldEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}

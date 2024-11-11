package org.habitsapp.contract;

import java.util.List;

public interface UserService {
    abstract String createToken(long id, String name, String email, String accessLevel);
    abstract String authorizeUser(String email, String password);
    abstract boolean registerUser(String name, String email, String password);
    abstract boolean logoutUser(Long id);
    abstract boolean deleteUser(Long userId, String password);
    abstract List<String> getUsersInfo(Long id);
    abstract boolean manageUserProfile(Long id, String emailToManage, String action);
    abstract boolean editUserData(Long id, String newEmail, String newName);
    abstract boolean editUserPassword(Long id, String oldPassword, String newPassword);
}

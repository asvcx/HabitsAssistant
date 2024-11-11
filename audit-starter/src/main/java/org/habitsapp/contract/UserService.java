package org.habitsapp.contract;

import java.util.List;

public interface UserService {
    String createToken(long id, String name, String email, String accessLevel);
    String authorizeUser(String email, String password);
    boolean registerUser(String name, String email, String password);
    boolean logoutUser(Long id);
    boolean deleteUser(Long userId, String password);
    List<String> getUsersInfo(Long id);
    boolean manageUserProfile(Long id, String emailToManage, String action);
    boolean editUserData(Long id, String newEmail, String newName);
    boolean editUserPassword(Long id, String oldPassword, String newPassword);
}

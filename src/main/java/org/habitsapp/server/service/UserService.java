package org.habitsapp.server.service;

import org.habitsapp.model.dto.UserDto;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.model.result.RegistrationResult;
import org.habitsapp.server.repository.ProfileAction;

import java.util.List;

public interface UserService {
    AuthorizationResult authorizeUser(String email, String password);
    RegistrationResult registerUser(UserDto userDTO);
    boolean logoutUser(String token);
    boolean deleteUser(String email, String token, String password);
    List<String> getUsersInfo(String email, String token);
    boolean manageUserProfile(String email, String token, String emailToManage, ProfileAction profileAction);
    boolean editUserData(String email, String token, String newEmail, String newName);
    boolean editUserPassword(String email, String token, String oldPassword, String newPassword);
}

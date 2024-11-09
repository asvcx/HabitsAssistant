package org.habitsapp.server.service;

import org.habitsapp.model.dto.UserDto;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.model.result.RegistrationResult;
import org.habitsapp.server.repository.ProfileAction;

import java.util.List;

public interface UserService {
    AuthorizationResult authorizeUser(String email, String password);
    RegistrationResult registerUser(UserDto userDTO);
    boolean logoutUser(Long id);
    boolean deleteUser(Long userId, String token, String password);
    List<String> getUsersInfo(Long id, String token);
    boolean manageUserProfile(Long id, String token, String emailToManage, ProfileAction profileAction);
    boolean editUserData(Long id, String token, String newEmail, String newName);
    boolean editUserPassword(Long id, String token, String oldPassword, String newPassword);
}

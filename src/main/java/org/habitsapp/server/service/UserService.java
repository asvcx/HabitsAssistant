package org.habitsapp.server.service;

import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.models.EntityStatus;

import java.security.SecureRandom;
import java.util.*;

import static org.habitsapp.client.in.UserInput.isEmailValid;
import static org.habitsapp.client.in.UserInput.isNameValid;

public class UserService {
    private final Repository repository;

    public UserService(Repository repository) {
        this.repository = repository;
    }

    private static String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public AuthorizationResult authorizeUser(String email, String password) {
        Optional<User> userOpt = repository.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            String wrongEmailMsg = String.format("User with email (%s) not found", email);
            return new AuthorizationResult(false, wrongEmailMsg, null, null);
        }
        User user = userOpt.get();
        if (user.isBlocked()) {
            return new AuthorizationResult(false, "User with specified email is blocked", null, null);
        }
        if (user.getAccountStatus() == EntityStatus.DELETED) {
            return new AuthorizationResult(false, "User with specified email is deleted", null, null);
        }
        if (!user.comparePassword(password)) {
            return new AuthorizationResult(false, "Specified password is wrong", null, null);
        }

        String token;
        do { token = generateToken();
        } while (repository.isTokenExists(token));

        UserDto userDTO = new UserDto(user.getName(), user.getEmail(), user.getPassword(), user.getAccessLevel());
        AuthorizationResult result = new AuthorizationResult(true, "Authentication successful", token, userDTO);
        repository.addToken(result.getToken(), user);
        return result;
    }

    public RegistrationResult registerUser(UserDto userDTO) {
        Optional<User> userOpt = repository.getUserByEmail(userDTO.getEmail());
        if (userOpt.isPresent()) {
            return new RegistrationResult(false, "User with specified email already exists");
        }
        if (userDTO.getPassword().length() < 6) {
            return new RegistrationResult(false, "Password must contain at least 6 characters");
        }
        if (!isEmailValid(userDTO.getEmail())) {
            return new RegistrationResult(false, "Specified email is not valid");
        }
        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getPassword());
        if (!repository.createUser(user)) {
            return new RegistrationResult(false, "Error occurred during registration");
        };
        return new RegistrationResult(true, "User registered successfully");
    }

    public boolean logoutUser(String token) {
        return repository.removeToken(token);
    }

    public boolean deleteUser(String email, String token, String password) {
        if (!repository.checkToken(email, token) || !repository.checkPassword(email, password)) {
            return false;
        }
        return repository.deleteUser(email, token);
    }

    public List<String> getUsersInfo(String email, String token) {
        if (email == null || token == null || !repository.isUserExists(email)) {
            return new LinkedList<>();
        }
        Optional<User> user = repository.getUserByToken(token);
        if (user.isEmpty() || !email.equals(user.get().getEmail())
                || user.get().getAccessLevel() != AccessLevel.ADMIN) {
            return new LinkedList<>();
        }
        List<User> userSet = repository.getUsers();
        return userSet.stream()
                .filter(u -> u.getAccountStatus() != EntityStatus.DELETED)
                .map(User::toString)
                .toList();
    }

    public boolean manageUserProfile(String email, String token, String emailToManage, Repository.ProfileAction profileAction) {
        Optional<User> adminOpt = repository.getUserByToken(token);
        if (adminOpt.isEmpty() || adminOpt.get().getAccessLevel() != AccessLevel.ADMIN
            || !repository.isUserExists(emailToManage)) {
            return false;
        }
        User user = repository.getUserByEmail(emailToManage).get();
        User admin = adminOpt.get();
        if (!email.equals(admin.getEmail())) {
            return false;
        }
        return switch (profileAction) {
            case Repository.ProfileAction.BLOCK -> {
                if (!user.isBlocked() && user.getAccountStatus() != EntityStatus.DELETED) {
                    user.block();
                    user.setAccountStatus(EntityStatus.UPDATED);
                    yield true;
                }
                yield false;
            }
            case Repository.ProfileAction.UNBLOCK -> {
                if (user.isBlocked()) {
                    user.unblock();
                    user.setAccountStatus(EntityStatus.UPDATED);
                    yield true;
                }
                yield false;
            }
            case Repository.ProfileAction.DELETE -> {
                user.setAccountStatus(EntityStatus.DELETED);
                yield true;
            }
        };
    }

    public boolean editUserData(String email, String token, String newEmail, String newName) {
        email = email.toLowerCase();
        if (!repository.checkToken(email, token) || newEmail == null || newName == null) {
            return false;
        }
        if (!isEmailValid(newEmail) || !isNameValid(newName)) {
            return false;
        }
        Optional<User> user = repository.getUserByEmail(email);
        if (user.isEmpty()) {
            return false;
        }
        user.get().setEmail(newEmail);
        user.get().setName(newName);
        return repository.replaceUser(email, token, user.get());
    }

    public boolean editUserPassword(String email, String token, String oldPassword, String newPassword) {
        if (!repository.checkPassword(email, oldPassword)
                || !repository.checkToken(email, token)) {
            return false;
        }
        Optional<User> user = repository.getUserByEmail(email.toLowerCase());
        if (user.isEmpty()) {
            return false;
        }
        if (user.get().comparePassword(oldPassword)) {
            user.get().setPassword(newPassword);
            return repository.replaceUser(email, token, user.get());
        } else {
            return false;
        }
    }

}

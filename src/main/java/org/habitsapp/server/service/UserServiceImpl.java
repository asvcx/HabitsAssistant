package org.habitsapp.server.service;

import io.jsonwebtoken.Claims;
import org.habitsapp.model.result.AuthorizationResult;
import org.habitsapp.model.result.RegistrationResult;
import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.User;
import org.habitsapp.model.dto.UserDto;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.model.EntityStatus;
import org.habitsapp.server.repository.ProfileAction;
import org.habitsapp.server.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.habitsapp.model.UserValidator.isEmailValid;
import static org.habitsapp.model.UserValidator.isNameValid;

@Service
public class UserServiceImpl implements UserService {
    private final AccountRepo repository;
    private final JwtService jwt;

    @Autowired
    public UserServiceImpl(AccountRepo repository, JwtService jwt) {
        this.repository = repository;
        this.jwt = jwt;
    }

    public String createToken(User user) {
        Map<String,String> payload = new HashMap<>();
        payload.put("email", user.getEmail().toLowerCase());
        payload.put("access", user.getAccessLevel().name());
        return jwt.generateJwt(payload, user.getName(), String.valueOf(user.getId()));
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
        if (!user.comparePassword(password)) {
            return new AuthorizationResult(false, "Specified password is wrong", null, null);
        }
        String token = createToken(user);
        UserDto userDTO = new UserDto(user.getName(), user.getEmail(), user.getPassword(), user.getAccessLevel());
        AuthorizationResult result = new AuthorizationResult(true, "Authentication successful", token, userDTO);
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
        // return repository.removeToken(token);
        return true;
    }

    public boolean deleteUser(String email, String token, String password) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || !repository.checkPassword(email, password)) {
            return false;
        }
        return repository.deleteUser(email, token);
    }

    public List<String> getUsersInfo(String email, String token) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || email == null || token == null || !repository.isUserExists(email)) {
            return new LinkedList<>();
        }
        Optional<User> user = repository.getUserByEmail(email);
        if (user.isEmpty() || !email.equals(user.get().getEmail())
                || user.get().getAccessLevel() != AccessLevel.ADMIN) {
            return new LinkedList<>();
        }
        List<User> userSet = repository.getUsers();
        return userSet.stream()
                //.filter(u -> u.getAccountStatus() != EntityStatus.DELETED)
                .map(User::toString)
                .toList();
    }

    public boolean manageUserProfile(String email, String token, String emailToManage, ProfileAction profileAction) {
        Claims claims = jwt.extractClaims(token);
        Optional<User> adminOpt = repository.getUserByEmail(email);
        Optional<User> userOpt = repository.getUserByEmail(emailToManage);
        if (claims == null || adminOpt.isEmpty() || userOpt.isEmpty()
            || adminOpt.get().getAccessLevel() != AccessLevel.ADMIN) {
            return false;
        }
        User user = userOpt.get();
        User admin = adminOpt.get();
        if (!email.equals(admin.getEmail())) {
            return false;
        }
        return switch (profileAction) {
            case ProfileAction.BLOCK -> {
                if (!user.isBlocked()) {
                    yield repository.updateUser(emailToManage, User::block);
                }
                yield false;
            }
            case ProfileAction.UNBLOCK -> {
                if (user.isBlocked()) {
                    yield repository.updateUser(emailToManage, User::unblock);
                }
                yield false;
            }
            case ProfileAction.DELETE -> repository.deleteUser(emailToManage, "");
        };
    }

    public boolean editUserData(String email, String token, String newEmail, String newName) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || newEmail == null || newName == null) {
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
        return repository.updateUser(email, token, user.get());
    }

    public boolean editUserPassword(String email, String token, String oldPassword, String newPassword) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || !repository.checkPassword(email, oldPassword)) {
            return false;
        }
        Optional<User> user = repository.getUserByEmail(email.toLowerCase());
        if (user.isEmpty()) {
            return false;
        }
        if (user.get().comparePassword(oldPassword)) {
            user.get().setPassword(newPassword);
            return repository.updateUser(email, token, user.get());
        } else {
            return false;
        }
    }

}

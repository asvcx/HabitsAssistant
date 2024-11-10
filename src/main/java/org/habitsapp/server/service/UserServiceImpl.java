package org.habitsapp.server.service;

import io.jsonwebtoken.Claims;
import org.example.UserService;
import org.habitsapp.model.AccessLevel;
import org.habitsapp.model.User;
import org.habitsapp.server.repository.AccountRepo;
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

    public String authorizeUser(String email, String password) {
        Optional<User> userOpt = repository.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return "";
        }
        User user = userOpt.get();
        if (user.isBlocked()) {
            return "";
        }
        if (!user.comparePassword(password)) {
            return "";
        }
        return createToken(user);
    }

    public boolean registerUser(String name, String email, String password) {
        Optional<User> userOpt = repository.getUserByEmail(email);
        if (userOpt.isPresent()) {
            return false;
        }
        if (password.length() < 6) {
            return false;
        }
        if (!isEmailValid(email)) {
            return false;
        }
        User user = new User(name, email, password);
        if (!repository.createUser(user)) {
            return false;
        };
        return true;
    }

    public boolean logoutUser(Long id) {
        // return repository.removeToken(token);
        return true;
    }

    public boolean deleteUser(Long userId, String token, String password) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || !repository.checkPassword(userId, password)) {
            return false;
        }
        return repository.deleteUser(userId);
    }

    public List<String> getUsersInfo(Long userId, String token) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || userId == null || token == null || !repository.isUserExists(userId)) {
            return new LinkedList<>();
        }
        Optional<User> user = repository.getUserById(userId);
        if (user.isEmpty() || !userId.equals(user.get().getId())
                || user.get().getAccessLevel() != AccessLevel.ADMIN) {
            return new LinkedList<>();
        }
        List<User> userSet = repository.getUsers();
        return userSet.stream()
                //.filter(u -> u.getAccountStatus() != EntityStatus.DELETED)
                .map(User::toString)
                .toList();
    }

    public boolean manageUserProfile(Long adminId, String token, String emailToManage, String action) {
        Claims claims = jwt.extractClaims(token);
        Optional<User> adminOpt = repository.getUserById(adminId);
        Optional<User> userOpt = repository.getUserByEmail(emailToManage);
        if (claims == null || adminOpt.isEmpty() || userOpt.isEmpty()
            || adminOpt.get().getAccessLevel() != AccessLevel.ADMIN) {
            return false;
        }
        User user = userOpt.get();
        User admin = adminOpt.get();
        if (!adminId.equals(admin.getId())) {
            return false;
        }
        ProfileAction profileAction;
        switch (action.toLowerCase()) {
            case "unblock" : {
                profileAction = ProfileAction.UNBLOCK;
                break;
            }
            case "block" : {
                profileAction = ProfileAction.BLOCK;
                break;
            }
            case "delete" : {
                profileAction = ProfileAction.DELETE;
                break;
            }
            default: {
                return  false;
            }
        }
        return switch (profileAction) {
            case ProfileAction.BLOCK -> {
                if (!user.isBlocked()) {
                    yield repository.updateUser(user.getId(), User::block);
                }
                yield false;
            }
            case ProfileAction.UNBLOCK -> {
                if (user.isBlocked()) {
                    yield repository.updateUser(user.getId(), User::unblock);
                }
                yield false;
            }
            case ProfileAction.DELETE -> repository.deleteUser(user.getId());
        };
    }

    public boolean editUserData(Long userId, String token, String newEmail, String newName) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || newEmail == null || newName == null) {
            return false;
        }
        if (!isEmailValid(newEmail) || !isNameValid(newName)) {
            return false;
        }
        Optional<User> user = repository.getUserById(userId);
        if (user.isEmpty()) {
            return false;
        }
        user.get().setEmail(newEmail);
        user.get().setName(newName);
        return repository.updateUser(userId, token, user.get());
    }

    public boolean editUserPassword(Long userId, String token, String oldPassword, String newPassword) {
        Claims claims = jwt.extractClaims(token);
        if (claims == null || !repository.checkPassword(userId, oldPassword)) {
            return false;
        }
        Optional<User> user = repository.getUserById(userId);
        if (user.isEmpty()) {
            return false;
        }
        if (user.get().comparePassword(oldPassword)) {
            user.get().setPassword(newPassword);
            return repository.updateUser(userId, token, user.get());
        } else {
            return false;
        }
    }

}

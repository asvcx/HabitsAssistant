package org.habitsapp.server.service;

import org.habitsapp.contract.UserService;
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

    public String createToken(long id, String name, String email, String accessLevel) {
        Map<String,String> payload = new HashMap<>();
        payload.put("email", email.toLowerCase());
        payload.put("access", accessLevel);
        return jwt.generateJwt(payload, name, String.valueOf(id));
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
        return createToken(user.getId(), user.getName(), email, user.getAccessLevel().name());
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

    public boolean deleteUser(Long userId, String password) {
        if (!repository.checkPassword(userId, password)) {
            return false;
        }
        return repository.deleteUser(userId);
    }

    public List<String> getUsersInfo(Long userId) {
        if (userId == null || !repository.isUserExists(userId)) {
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

    public boolean manageUserProfile(Long adminId, String emailToManage, String action) {
        Optional<User> adminOpt = repository.getUserById(adminId);
        Optional<User> userOpt = repository.getUserByEmail(emailToManage);
        if (adminOpt.isEmpty() || userOpt.isEmpty()
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
                    yield repository.setUserBlockStatus(user.getId(), User::block);
                }
                yield false;
            }
            case ProfileAction.UNBLOCK -> {
                if (user.isBlocked()) {
                    yield repository.setUserBlockStatus(user.getId(), User::unblock);
                }
                yield false;
            }
            case ProfileAction.DELETE -> repository.deleteUser(user.getId());
        };
    }

    public boolean editUserData(Long userId, String newEmail, String newName) {
        if (newEmail == null || newName == null) {
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
        return repository.updateUser(userId, user.get());
    }

    public boolean editUserPassword(Long userId, String oldPassword, String newPassword) {
        if (!repository.checkPassword(userId, oldPassword)) {
            return false;
        }
        Optional<User> user = repository.getUserById(userId);
        if (user.isEmpty()) {
            return false;
        }
        if (user.get().comparePassword(oldPassword)) {
            user.get().setPassword(newPassword);
            return repository.updateUser(userId, user.get());
        } else {
            return false;
        }
    }

}

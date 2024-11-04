package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.models.results.RegistrationResult;
import org.habitsapp.exchange.ProfileChangeDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.UserDto;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.UserService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/api/profile")
public class ProfileServlet extends HttpServlet {
    ApplicationContext appContext;
    UserService userService;
    Repository repository;
    DtoReader dtoReader;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appContext = (ApplicationContext) getServletContext().getAttribute("appContext");
        userService = appContext.getUserService();
        repository = appContext.getRepository();
        dtoReader = new DtoReader();
    }

    /**
     *  Create user profile
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDTO;
        // Check dto
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect user data format"));
            return;
        }
        if(userDTO.getEmail() == null || userDTO.getPassword() == null || userDTO.getName() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("User data have not been provided"));
        }
        // Create user profile
        RegistrationResult result = userService.registerUser(userDTO);
        if (result.isSuccess()) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto(result.getMessage()));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto(result.getMessage()));
        }
    }

    /**
     *  Change user profile
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = dtoReader.readToken(req, resp, repository);
        if (token == null || token.isEmpty()) {
            return;
        }
        // Check dto
        ProfileChangeDto usrChange;
        try {
            usrChange = objectMapper.readValue(req.getInputStream(), ProfileChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect dto format"));
            return;
        }
        // Change user profile
        Optional<User> user = repository.getUserByToken(token);
        boolean isChanged = user.isPresent() && userService.editUserData(
                usrChange.getOldEmail(), token, usrChange.getNewEmail(), usrChange.getNewName()
        );
        if (isChanged) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Profile changed successfully"));
        } else {
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to change profile"));
        }
    }

    /**
     *  Delete user profile
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = dtoReader.readToken(req, resp, repository);
        String password = req.getHeader("X-Confirm-Password");
        if (token == null || token.isEmpty() || password == null || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Bad request"));
            return;
        }
        // Delete user profile
        Optional<User> user = repository.getUserByToken(token);
        boolean isDeleted = user.isPresent()
                && userService.deleteUser(user.get().getEmail(), token, user.get().getPassword());
        if(isDeleted) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Profile deleted successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to delete profile"));
        }
    }
}

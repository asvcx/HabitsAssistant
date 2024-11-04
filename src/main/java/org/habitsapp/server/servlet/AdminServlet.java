package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.habitsapp.exchange.AdminActionDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.User;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {
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
     *  Get list of users by admin
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = dtoReader.readToken(req, resp, repository);
        Optional<User> admin = repository.getUserByToken(token);
        if (token == null || token.isEmpty() || admin.isEmpty()) {
            return;
        }
        List<String> usersInfo = userService.getUsersInfo(admin.get().getEmail(), token);
        if(admin.get().getAccessLevel() == AccessLevel.ADMIN) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), usersInfo);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to delete user"));
        }
    }

    /**
     *  Manage user profile by admin
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        AdminActionDto actionDto;
        // Check request
        String token = dtoReader.readToken(req, resp, repository);
        try {
            actionDto = objectMapper.readValue(req.getInputStream(), AdminActionDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect user data format"));
            return;
        }
        if (token == null || token.isEmpty() || actionDto == null
                || actionDto.getProfileAction() == null || actionDto.getEmailToManage() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Bad request"));
            return;
        }
        // Manage user profile
        String emailToManage = actionDto.getEmailToManage();
        Optional<User> admin = repository.getUserByToken(token);
        Optional<User> user = repository.getUserByEmail(emailToManage);
        Repository.ProfileAction profileAction = actionDto.getProfileAction();

        boolean isManaged = admin.isPresent() && user.isPresent()
                && userService.manageUserProfile(admin.get().getEmail(), token, emailToManage, profileAction);
        if(isManaged) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Action performed successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to perform user"));
        }
    }


}

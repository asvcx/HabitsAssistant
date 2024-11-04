package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.PasswordChangeDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.User;
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

@WebServlet("/api/password")
public class PasswordServlet extends HttpServlet {
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
     *  Change user password
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = dtoReader.readToken(req, resp, repository);
        PasswordChangeDto pswChange = dtoReader.readPswChangeDto(req, resp);
        if (token == null || token.isEmpty() || pswChange == null) {
            return;
        }
        // Find user
        Optional<User> user = repository.getUserByToken(token);
        // Change user password
        boolean isChanged = user.isPresent() && userService.editUserPassword(
                pswChange.getUserEmail(), token, pswChange.getOldPassword(), pswChange.getNewPassword()
        );
        if (isChanged) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Password changed successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to change password"));
        }
    }
}

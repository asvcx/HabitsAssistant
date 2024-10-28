package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.annotations.Measurable;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.PasswordChangeDto;
import org.habitsapp.exchange.ResponseDto;
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

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appContext = (ApplicationContext) getServletContext().getAttribute("appContext");
        userService = appContext.getUserService();
        repository = appContext.getRepository();
    }

    /**
     *  Change user password
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        // Check token
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Bad request"));
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Check dto
        PasswordChangeDto pswChange;
        try {
            pswChange = objectMapper.readValue(req.getInputStream(), PasswordChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Incorrect dto format"));
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
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Password changed successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Something gone wrong"));
        }
    }
}

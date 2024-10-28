package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.annotations.Measurable;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.ResponseDto;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.UserService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {
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
     * Logout from profile
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Check authorization
        if (!repository.isUserAuthorized(token)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        if (userService.logoutUser(token)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Successfully logged out"));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
        }
    }
}

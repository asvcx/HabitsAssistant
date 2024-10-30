package org.habitsapp.server.servlet;

import org.habitsapp.exchange.SessionDto;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.User;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.UserService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
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
     * Login to profile
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDTO;
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect user data format"));
            return;
        }
        AuthorizationResult result = userService.authorizeUser(userDTO.getEmail(), userDTO.getPassword());
        if (result.isSuccess()) {
            UserDto userDto = result.getUserDto();
            resp.setHeader("Authorization", "Token " + result.getToken());
            resp.setStatus(HttpServletResponse.SC_OK);
            SessionDto sessionDto = new SessionDto(userDto.getName(), userDto.getEmail(), userDto.getAccessLevel());
            objectMapper.writeValue(resp.getOutputStream(), sessionDto);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto(result.getMessage()));
        }
    }

}

package org.habitsapp.server.servlet;

import org.habitsapp.annotations.Measurable;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.ResponseDto;
import org.habitsapp.models.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.client.session.AuthorizationResult;
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
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Invalid user data format"));
            return;
        }
        AuthorizationResult result = userService.authorizeUser(userDTO.getEmail(), userDTO.getPassword());
        if (result.getSuccess()) {
            resp.setHeader("Authorization", "Token " + result.getToken());
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        }
    }

}

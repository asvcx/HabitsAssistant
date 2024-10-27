package habitsapp.server.servlet;

import habitsapp.annotations.Auditable;
import habitsapp.server.ApplicationContext;
import habitsapp.exchange.ResponseDto;
import habitsapp.models.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import habitsapp.client.session.AuthorizationResult;
import habitsapp.server.repository.Repository;
import habitsapp.server.service.UserService;
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
    @Auditable
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Попытка авторизации");
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDTO;
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDto.class);
        } catch (IOException e) {
            System.out.println("Не удалось прочитать UserDto");
            resp.setContentType("application/json");
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

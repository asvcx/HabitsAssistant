package habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitsapp.annotations.Auditable;
import habitsapp.server.ApplicationContext;
import habitsapp.exchange.ResponseDto;
import habitsapp.server.repository.Repository;
import habitsapp.server.service.UserService;
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

    @Auditable
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
        System.out.println("Попытка выхода");
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            System.out.println("Некорректный токен");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Check authorization
        if (!repository.isUserAuthorized(token)) {
            System.out.println("Пользователь не авторизован");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        if (userService.logoutUser(token)) {
            System.out.println("Пользователь вышел");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Successfully logged out"));
        } else {
            System.out.println("Не удалось выполнить выход");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
        }
    }
}

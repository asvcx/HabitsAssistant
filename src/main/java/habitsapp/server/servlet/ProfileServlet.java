package habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitsapp.annotations.Auditable;
import habitsapp.annotations.Measurable;
import habitsapp.server.ApplicationContext;
import habitsapp.client.session.RegistrationResult;
import habitsapp.exchange.ProfileChangeDto;
import habitsapp.exchange.ResponseDto;
import habitsapp.models.User;
import habitsapp.models.dto.UserDto;
import habitsapp.server.repository.Repository;
import habitsapp.server.service.UserService;
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

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appContext = (ApplicationContext) getServletContext().getAttribute("appContext");
        userService = appContext.getUserService();
        repository = appContext.getRepository();
    }

    /**
     *  Create user profile
     */
    @Auditable
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Попытка создания профиля");
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDTO;
        // Check dto
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDto.class);
        } catch (IOException e) {
            System.out.println("Некорректный userDto");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Invalid user data format"));
            return;
        }
        if(userDTO.getEmail() == null || userDTO.getPassword() == null || userDTO.getName() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("User data have not been provided"));
        }
        // Create user profile
        RegistrationResult result = userService.registerUser(userDTO);
        if (result.isSuccess()) {
            System.out.println("Профиль создан: " + userDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        } else {
            System.out.println("Не удалось создать профиль: " + userDTO);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        }
    }

    /**
     *  Change user profile
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Попытка изменения профиля");
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        // Check token
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Bad request"));
            return;
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Check dto
        ProfileChangeDto usrChange;
        try {
            usrChange = objectMapper.readValue(req.getInputStream(), ProfileChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Invalid dto format"));
            return;
        }
        // Find user
        Optional<User> user = repository.getUserByToken(token);
        // Change user profile
        boolean isChanged = user.isPresent() && userService.editUserData(
                usrChange.getOldEmail(), token, usrChange.getNewName(), usrChange.getNewName()
        );
        if (isChanged) {
            System.out.println("Профиль успешно изменен");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Profile changed successfully"));
        } else {
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Something gone wrong"));
        }
    }

    /**
     *  Delete user profile
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Попытка удаления профиля");
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        // Check token and password
        String token = req.getHeader("Authorization");
        String password = req.getHeader("X-Confirm-Password");
        if (token == null || token.isEmpty() || password == null || password.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Bad request"));
            return;
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Find user
        Optional<User> user = repository.getUserByToken(token);
        // Delete user profile
        boolean isDeleted = user.isPresent()
                && userService.deleteUser(user.get().getEmail(), token, user.get().getPassword());
        if(isDeleted) {
            System.out.println("Профиль успешно удален");
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Profile deleted successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Something gone wrong"));
        }
    }
}

package habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitsapp.annotations.Auditable;
import habitsapp.server.ApplicationContext;
import habitsapp.client.session.RegistrationResult;
import habitsapp.exchange.HabitChangeDto;
import habitsapp.exchange.HabitsListDto;
import habitsapp.exchange.ResponseDto;
import habitsapp.models.Habit;
import habitsapp.models.User;
import habitsapp.models.dto.HabitDto;
import habitsapp.models.dto.HabitMapper;
import habitsapp.models.dto.UserDto;
import habitsapp.server.repository.Repository;
import habitsapp.server.service.HabitService;
import habitsapp.server.service.UserService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/habits")
public class HabitServlet extends HttpServlet {
    ApplicationContext appContext;
    UserService userService;
    HabitService habitService;
    Repository repository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appContext = (ApplicationContext) getServletContext().getAttribute("appContext");
        userService = appContext.getUserService();
        habitService = appContext.getHabitService();
        repository = appContext.getRepository();
    }

    /**
     *  Get list of habits
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        // Check token
        String token = req.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
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
        }
        Optional<User> user = repository.getUserByToken(token);
        List<Habit> habits = repository.getHabitsOfUser(user.get().getEmail()).stream().toList();
        List<HabitDto> habitsDto = habits.stream().map(HabitMapper.INSTANCE::habitToHabitDto).toList();
        HabitsListDto habitsList = new HabitsListDto(habitsDto);
        boolean isProvided = repository.isUserAuthorized(token)
                && user.isPresent();
        if(isProvided) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Habit deleted successfully");
            objectMapper.writeValue(resp.getOutputStream(), habitsList);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Something gone wrong");
        }
    }

    /**
     *  Create new habit
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDTO;
        String token = req.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        if (token.startsWith("Token ")) {
            token = token.substring(6);
        }
        // Check dto
        try {
            userDTO = objectMapper.readValue(req.getInputStream(), UserDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Invalid habit data format"));
            return;
        }
        if(userDTO.getEmail() == null || userDTO.getPassword() == null || userDTO.getName() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit have not been provided"));
        }
        // Create habit
        RegistrationResult result = userService.registerUser(userDTO);
        if (result.isSuccess()) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto(result.getMessage()));
        }
    }

    /**
     *  Change existing habit
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        // Check token
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
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
        }
        // Check dto
        HabitChangeDto hbtChange;
        try {
            hbtChange = objectMapper.readValue(req.getInputStream(), HabitChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Invalid dto format"));
            return;
        }
        // Change habit
        Optional<User> user = repository.getUserByToken(token);
        Habit oldHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getOldHabit());
        Habit newHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getNewHabit());
        boolean isChanged = habitService.editHabit(
                user.get().getEmail(), token, oldHabit, newHabit
        );
        if (isChanged) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit changed successfully"));
        } else {
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Something gone wrong"));
        }
    }

    /**
     *  Delete existing habit
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        // Check token
        String token = req.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
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
        }
        // Check habit title
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit title has not been provided"));
            return;
        }
        String habitTitle = pathInfo.substring(1);
        Optional<User> user = repository.getUserByToken(token);
        boolean isDeleted = repository.isUserAuthorized(token)
                && user.isPresent()
                && habitService.deleteHabit(user.get().getEmail(), token, habitTitle);
        if(isDeleted) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Habit deleted successfully");
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Something gone wrong");
        }
    }
}

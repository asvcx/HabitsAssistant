package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.client.session.HabitCreationResult;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.HabitChangeDto;
import org.habitsapp.exchange.HabitsListDto;
import org.habitsapp.exchange.ResponseDto;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.server.repository.Repository;
import org.habitsapp.server.service.HabitService;
import org.habitsapp.server.service.UserService;
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
        String token = readToken(req, resp);
        Optional<User> user = repository.getUserByToken(token);
        if (user.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        // Get list
        List<Habit> habits = repository.getHabitsOfUser(user.get().getEmail()).stream().toList();
        List<HabitDto> habitsDto = habits.stream()
                .map(HabitMapper.INSTANCE::habitToHabitDto)
                .toList();
        HabitsListDto habitsList = new HabitsListDto(habitsDto);
        boolean isAuthorized = repository.isUserAuthorized(token);
        // Respond
        if(isAuthorized) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), habitsList);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Something gone wrong"));
        }
    }

    /**
     *  Create a new habit
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = readToken(req, resp);
        Optional<User> user = repository.getUserByToken(token);
        HabitDto habitDto = readHabitDto(req, resp);
        if(habitDto == null || user.isEmpty()) {
            return;
        }
        HabitCreationResult result = habitService.createHabit(user.get().getEmail(), token, habitDto);
        if (result.getSuccess()) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit has been created"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit has been created"));
        }
    }

    /**
     *  Change existing habit
     */
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String token = readToken(req, resp);
        // Check token
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
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
        String token = readToken(req, resp);
        if (token == null || token.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return;
        }
        // Check habit title
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            System.out.println("Не найдено название привыки");
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
            System.out.println("Привычка удалена. Возвращение ответа.");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Habit deleted successfully");
        } else {
            System.out.println("Не удалось удалить привычку.");
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Something gone wrong");
        }
    }

    public String readToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Token ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("You have not been authorized"));
            return null;
        }
        String tokenValue = token.substring(6);
        return repository.isUserAuthorized(tokenValue) ? tokenValue : null;
    }

    public HabitDto readHabitDto(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HabitDto habitDto;
        try {
            habitDto = objectMapper.readValue(req.getInputStream(), HabitDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Incorrect habit data format"));
            return null;
        }
        if(habitDto.getTitle() == null || habitDto.getDescription() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new ResponseDto("Habit have not been provided"));
            return null;
        }
        return habitDto;
    }
}

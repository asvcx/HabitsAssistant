package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.habitsapp.models.results.HabitCreationResult;
import org.habitsapp.server.ApplicationContext;
import org.habitsapp.exchange.HabitChangeDto;
import org.habitsapp.exchange.MessageDto;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/api/habits")
public class HabitServlet extends HttpServlet {
    ApplicationContext appContext;
    UserService userService;
    HabitService habitService;
    Repository repository;
    DtoReader dtoReader;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        appContext = (ApplicationContext) getServletContext().getAttribute("appContext");
        userService = appContext.getUserService();
        habitService = appContext.getHabitService();
        repository = appContext.getRepository();
        dtoReader = new DtoReader();
    }

    /**
     *  Get list of habits
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        // Check token
        String token = dtoReader.readToken(req, resp, repository);
        Optional<User> user = repository.getUserByToken(token);
        if (user.isEmpty()) {
            return;
        }
        // Get list of habits
        Set<HabitDto> habitsDto = repository.getHabitsOfUser(user.get().getEmail())
                .stream()
                .map(HabitMapper.INSTANCE::habitToHabitDto)
                .collect(Collectors.toSet());

        boolean isAuthorized = repository.isUserAuthorized(token);
        if(isAuthorized) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), habitsDto);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to return habits list"));
        }
    }

    /**
     *  Create a new habit
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        String token = dtoReader.readToken(req, resp, repository);
        Optional<User> user = repository.getUserByToken(token);
        HabitDto habitDto = dtoReader.readHabitDto(req, resp);
        if(habitDto == null || user.isEmpty()) {
            return;
        }
        HabitCreationResult result = habitService.createHabit(user.get().getEmail(), token, habitDto);
        if (result.getSuccess()) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habit has been created"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to create a habit"));
        }
    }

    /**
     *  Change existing habit
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        String token = dtoReader.readToken(req, resp, repository);
        HabitChangeDto hbtChange = dtoReader.readHbtChangeDto(req, resp);
        if (token == null || token.isEmpty() || hbtChange == null) {
            return;
        }
        // Change habit
        Optional<User> user = repository.getUserByToken(token);
        Habit oldHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getOldHabit());
        Habit newHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getNewHabit());
        boolean isChanged = user.isPresent() &&
                habitService.editHabit(user.get().getEmail(), token, oldHabit, newHabit);
        if (isChanged) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habit changed successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Something gone wrong"));
        }
    }

    /**
     *  Delete existing habit
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        resp.setContentType("application/json");
        // Check token
        String token = dtoReader.readToken(req, resp, repository);
        if (token == null || token.isEmpty()) {
            return;
        }
        // Check habit title
        String habitTitle = req.getParameter("title");
        if (habitTitle == null || habitTitle.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habit title has not been provided"));
            return;
        }
        Optional<User> user = repository.getUserByToken(token);
        boolean isDeleted = user.isPresent()
                && habitService.deleteHabit(user.get().getEmail(), token, habitTitle);
        if(isDeleted) {
            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habit deleted successfully"));
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Failed to delete the habit"));
        }
    }

}

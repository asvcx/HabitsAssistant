package org.habitsapp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.habitsapp.exchange.HabitChangeDto;
import org.habitsapp.exchange.PasswordChangeDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.server.repository.Repository;

import java.io.IOException;

public class DtoReader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DtoReader() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
    }

    public String readToken(HttpServletRequest req, HttpServletResponse resp, Repository repo) throws IOException {
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Token ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("You have not been authorized"));
            return null;
        }
        String tokenValue = token.substring(6);
        return repo.isUserAuthorized(tokenValue) ? tokenValue : null;
    }

    public HabitDto readHabitDto(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HabitDto habitDto;
        try {
            habitDto = objectMapper.readValue(req.getInputStream(), HabitDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect habit data format"));
            return null;
        }
        if(habitDto.getTitle() == null || habitDto.getDescription() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habit has not been provided"));
            return null;
        }
        return habitDto;
    }

    public HabitChangeDto readHbtChangeDto(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HabitChangeDto hbtChange;
        try {
            hbtChange = objectMapper.readValue(req.getInputStream(), HabitChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect dto format"));
            return null;
        }
        if ((hbtChange.getOldHabit() == null) || (hbtChange.getNewHabit() == null)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Habits has not been provided"));
            return null;
        }
        return hbtChange;
    }

    public PasswordChangeDto readPswChangeDto(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PasswordChangeDto pswChange;
        try {
            pswChange = objectMapper.readValue(req.getInputStream(), PasswordChangeDto.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect dto format"));
            return null;
        }
        if (pswChange.getUserEmail() == null || pswChange.getOldPassword() == null || pswChange.getNewPassword() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getOutputStream(), new MessageDto("Incorrect password change dto"));
            return null;
        }
        return pswChange;
    }





}

package org.habitsapp.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.habitsapp.exchange.HabitChangeDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.models.Habit;
import org.habitsapp.models.User;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.models.results.HabitCreationResult;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habits")
public class HabitController {
    private final HabitService habitService;
    private final AccountRepo repository;

    @Autowired
    public HabitController(HabitService habitService, AccountRepo repository) {
        this.habitService = habitService;
        this.repository = repository;
    }

    /**
     *  Get list of habits
     */
    @GetMapping
    protected ResponseEntity<Set<HabitDto>> getHabitsList(HttpServletRequest req) {
        // Check token
        String token = TokenReader.readToken(req, repository);
        Optional<User> user = repository.getUserByToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to get the list of habits
        Set<HabitDto> habitsDto = repository.getHabitsOfUser(user.get().getEmail())
                .map(habits -> habits.values().stream()
                        .map(HabitMapper.INSTANCE::habitToHabitDto)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        if(repository.isUserAuthorized(token)) {
            return ResponseEntity.ok().body(habitsDto);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     *  Create a new habit
     */
    @PostMapping
    protected ResponseEntity<MessageDto> createHabit(@RequestBody HabitDto habitDto, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        Optional<User> user = repository.getUserByToken(token);
        if(habitDto == null || user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to create habit
        HabitCreationResult result = habitService.createHabit(user.get().getEmail(), token, habitDto);
        if (result.success()) {
            return ResponseEntity.ok().body(new MessageDto("Habit has been created"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto("Failed to create a habit"));
        }
    }

    /**
     *  Change existing habit
     */
    @PutMapping
    protected ResponseEntity<MessageDto> changeHabit(@RequestBody HabitChangeDto hbtChange, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty() || hbtChange == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to change habit
        Optional<User> user = repository.getUserByToken(token);
        Habit oldHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getOldHabit());
        Habit newHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getNewHabit());
        boolean isChanged = user.isPresent() &&
                habitService.editHabit(user.get().getEmail(), token, oldHabit, newHabit);
        if (isChanged) {
            return ResponseEntity.ok().body(new MessageDto("Habit changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageDto("Habit changed successfully"));
        }
    }

    /**
     *  Change existing habit
     */
    @PutMapping("/mark")
    protected ResponseEntity<MessageDto> markHabit(HttpServletRequest req) {
        // Check token
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check habit title
        String habitTitle = req.getParameter("title");
        if (habitTitle == null || habitTitle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageDto("Habit title has not been provided"));
        }
        Optional<User> user = repository.getUserByToken(token);
        boolean isMarked = user.isPresent()
                && habitService.markHabitAsCompleted(user.get().getEmail(), token, habitTitle);
        if(isMarked) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Habit marked successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageDto("Failed to mark the habit"));
        }
    }

    /**
     *  Delete existing habit
     */
    @DeleteMapping
    protected ResponseEntity<MessageDto> deleteHabit(@RequestParam("title") String habitTitle, HttpServletRequest req) {
        // Check token
        String token = TokenReader.readToken(req, repository);
        Optional<User> user = repository.getUserByToken(token);
        if (token == null || token.isEmpty() || user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check habit title
        if (habitTitle == null || habitTitle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageDto("Habit title has not been provided"));
        }
        // Try to delete the habit
        String email = user.get().getEmail();
        boolean isDeleted = habitService.deleteHabit(email, token, habitTitle);
        if(isDeleted) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Habit deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageDto("Failed to delete the habit"));
        }
    }

}

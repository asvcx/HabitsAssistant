package org.habitsapp.server.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.habitsapp.exchange.HabitChangeDto;
import org.habitsapp.exchange.MessageDto;
import org.habitsapp.model.User;
import org.habitsapp.model.dto.HabitDto;
import org.habitsapp.model.dto.HabitMapper;
import org.habitsapp.model.result.HabitCreationResult;
import org.habitsapp.server.repository.AccountRepo;
import org.habitsapp.server.service.HabitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class Habit {
    private final HabitService habitService;
    private final AccountRepo repository;

    /**
     *  Get list of habits
     */
    @GetMapping
    protected ResponseEntity<Set<HabitDto>> getAll(HttpServletRequest req) {
        // Check token
        String token = TokenReader.readToken(req, repository);
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to get the list of habits
        Set<HabitDto> habitsDto = repository.getHabitsOfUser(id)
                .map(habits -> habits.values().stream()
                        .map(HabitMapper.INSTANCE::habitToHabitDto)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        return ResponseEntity.ok().body(habitsDto);
    }

    /**
     *  Create a new habit
     */
    @PostMapping
    protected ResponseEntity<MessageDto> create(@RequestBody HabitDto habitDto, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        if(habitDto == null || user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to create habit
        HabitCreationResult result = habitService.createHabit(id, token, habitDto);
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
    protected ResponseEntity<MessageDto> change(@RequestBody HabitChangeDto hbtChange, HttpServletRequest req) {
        String token = TokenReader.readToken(req, repository);
        if (token == null || token.isEmpty() || hbtChange == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Try to change habit
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        org.habitsapp.model.Habit oldHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getOldHabit());
        org.habitsapp.model.Habit newHabit = HabitMapper.INSTANCE.habitDtoToHabit(hbtChange.getNewHabit());
        boolean isChanged = user.isPresent() &&
                habitService.editHabit(id, token, oldHabit, newHabit);
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
    protected ResponseEntity<MessageDto> mark(HttpServletRequest req) {
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
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        boolean isMarked = user.isPresent()
                && habitService.markHabitAsCompleted(id, token, habitTitle);
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
    protected ResponseEntity<MessageDto> delete(@RequestParam("title") String habitTitle, HttpServletRequest req) {
        // Check token
        String token = TokenReader.readToken(req, repository);
        long id = Long.parseLong((String)req.getAttribute("id"));
        Optional<User> user = repository.getUserById(id);
        if (token == null || token.isEmpty() || user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check habit title
        if (habitTitle == null || habitTitle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageDto("Habit title has not been provided"));
        }
        // Try to delete the habit
        boolean isDeleted = habitService.deleteHabit(id, token, habitTitle);
        if(isDeleted) {
            return ResponseEntity.ok()
                    .body(new MessageDto("Habit deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageDto("Failed to delete the habit"));
        }
    }

}

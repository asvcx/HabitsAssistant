package org.habitsapp.client.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.habitsapp.exchange.*;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.Habit;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.models.dto.UserDto;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;
import org.habitsapp.server.repository.AccountRepository;

import static java.net.HttpURLConnection.*;

public class Request {
    private static final String baseUrl = "http://localhost:8080/HabitsAssistant/api";

    public String getJsonFromUserDto(UserDto userDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(userDTO);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public String getJsonFromHabitDto(HabitDto habitDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(habitDTO);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    protected HttpURLConnection createConnection(String address, String method, String token, String json) throws IOException {
        URI uri = URI.create(address);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        if (token != null) {
            connection.setRequestProperty("Authorization", "Token " + token);
        }
        if (json != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        return connection;
    }

    public List<String> getProfilesList(String email, String token) {
        String path = baseUrl + "/admin";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        int responseCode;
        List<String> result = new LinkedList<>();
        try {
            HttpURLConnection connection = createConnection(path, "GET", token, null);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                result = objectMapper.readValue(connection.getInputStream(), new TypeReference<>() {});
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new LinkedList<>();
        }
        return result;
    }

    public boolean manageUserProfile(String email, String token, String emailToManage, AccountRepository.ProfileAction action) {
        String path = baseUrl + "/admin";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        AdminActionDto actionDto = new AdminActionDto(emailToManage, action);
        int responseCode;
        boolean result = false;
        try {
            String json = objectMapper.writeValueAsString(actionDto);
            HttpURLConnection connection = createConnection(path, "POST", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
                result = true;
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return result;
    }

    public AuthorizationResult login(String email, String password) {
        String path = baseUrl + "/login";
        UserDto userDto = new UserDto("", email, password, AccessLevel.USER);
        String json = getJsonFromUserDto(userDto);
        ObjectMapper objectMapper = new ObjectMapper();
        AuthorizationResult result;
        try {
            HttpURLConnection connection = createConnection(path, "POST", null, json);
            int responseCode = connection.getResponseCode();

            if (responseCode == HTTP_OK) {
                String token = connection.getHeaderField("Authorization");
                if (token.startsWith("Token ")) {
                    token = token.substring(6);
                }
                SessionDto response = objectMapper.readValue(connection.getInputStream(), SessionDto.class);
                userDto.setAccessLevel(response.getAccessLevel());
                userDto.setName(response.getUserName());
                result = new AuthorizationResult(true, "", token, userDto);
            } else {
                String msg = String.format("Ошибка %s", responseCode);
                result = new AuthorizationResult(false, msg, null, null);
            }
            connection.disconnect();
        } catch (IOException e) {
            result = new AuthorizationResult(false, "Не удалось получить ответ сервера", null, null);
        }
        return result;
    }

    public boolean logout(String token) {
        String path = baseUrl + "/logout";
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode = 0;
        try {
            HttpURLConnection connection = createConnection(path, "POST", Session.getToken(), null);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return responseCode == HTTP_OK;
    }

    public RegistrationResult register(String name, String email, String password) {
        String path = baseUrl + "/profile";
        UserDto userDto = new UserDto(name, email, password, AccessLevel.USER);
        String json = getJsonFromUserDto(userDto);
        ObjectMapper objectMapper = new ObjectMapper();
        RegistrationResult result;
        try {
            HttpURLConnection connection = createConnection(path, "POST", null, json);
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode == HTTP_OK || responseCode == HTTP_CREATED) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                result = new RegistrationResult(true, response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                result = new RegistrationResult(false, response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            result = new RegistrationResult(false, "Не удалось получить ответ сервера");
        }
        return result;
    }

    public boolean editUserData(String email, String token, String newName, String newEmail) {
        String path = baseUrl + "/profile";
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(new ProfileChangeDto(email, newEmail, newName));
            HttpURLConnection connection = createConnection(path, "PUT", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public boolean editUserPassword(String email, String token, String oldPassword, String newPassword) {
        String path = baseUrl + "/password";
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(new PasswordChangeDto(email, oldPassword, newPassword));
            HttpURLConnection connection = createConnection(path, "PUT", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public boolean deleteOwnProfile(String token, String password) {
        String path = baseUrl + "/profile";
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode;
        try {
            HttpURLConnection connection = createConnection(path, "DELETE", token, null);
            connection.setRequestProperty("X-Confirm-Password", password);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public boolean createHabit(String token, HabitDto habitDto) {
        String path = baseUrl + "/habits";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        int responseCode;
        try {
            String json = getJsonFromHabitDto(habitDto);
            HttpURLConnection connection = createConnection(path, "POST", Session.getToken(), json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK || responseCode == HTTP_CREATED) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK || responseCode == HTTP_CREATED;
    }

    public boolean editHabit(String token, HabitDto oldHabitDto, HabitDto newHabitDto) {
        String path = baseUrl + "/habits";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(new HabitChangeDto(oldHabitDto, newHabitDto));
            HttpURLConnection connection = createConnection(path, "PUT", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public boolean markHabit(String token, HabitDto habitDto) {
        String path = baseUrl + "/mark";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(habitDto);
            HttpURLConnection connection = createConnection(path, "POST", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public boolean deleteHabit(String token, HabitDto habitDto) {
        String path = baseUrl + "/habits?title=" + habitDto.getTitle();
        System.out.println("Path for delete habit: " + path);
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        int responseCode;
        try {
            HttpURLConnection connection = createConnection(path, "DELETE", token, null);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK || responseCode == HTTP_NO_CONTENT) {
                MessageDto response = objectMapper.readValue(connection.getInputStream(), MessageDto.class);
                System.out.println(response.getMessage());
            } else {
                MessageDto response = objectMapper.readValue(connection.getErrorStream(), MessageDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK || responseCode == HTTP_NO_CONTENT;
    }

    public Set<Habit> getHabits(String token) {
        String path = baseUrl + "/habits";
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        objectMapper.registerModule(module);
        try {
            HttpURLConnection connection = createConnection(path, "GET", token, null);
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                Set<HabitDto> habitsDto = objectMapper.readValue(connection.getInputStream(), new TypeReference<>() {});
                return habitsDto.stream()
                        .map(HabitMapper.INSTANCE::habitDtoToHabit)
                        .collect(Collectors.toSet());
            } else {
                System.out.println("Не удалось загрузить привычки");
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new TreeSet<>();
        }
        return new TreeSet<>();
    }

}

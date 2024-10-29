package org.habitsapp.client.session;

import org.habitsapp.exchange.*;
import org.habitsapp.models.AccessLevel;
import org.habitsapp.models.Habit;
import org.habitsapp.models.dto.HabitDto;
import org.habitsapp.models.dto.HabitMapper;
import org.habitsapp.models.dto.UserDto;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.habitsapp.models.results.AuthorizationResult;
import org.habitsapp.models.results.RegistrationResult;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class Request {
    private static final String baseUrl = "http://localhost:8080/HabitsAssistant/api";

    private <T> T executeRequest(String path, String method, String jsonInput, Class<T> responseClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpURLConnection connection = createConnection(path, method, null, jsonInput);
            int responseCode = connection.getResponseCode();
            T response = null;
            if (responseCode == HTTP_OK) {
                response = objectMapper.readValue(connection.getInputStream(), responseClass);
            } else {
                response = objectMapper.readValue(connection.getErrorStream(), responseClass);
            }
            connection.disconnect();
            return response;
        } catch (IOException e) {
            System.out.println("Exception occurred while request execution: " + e.getMessage());
            return null;
        }
    }

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
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                result = new AuthorizationResult(true, response.getMessage(), token, userDto);
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
                result = new AuthorizationResult(false, response.getMessage(), null, null);
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
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
            if (responseCode == HTTP_OK || responseCode == HTTP_CREATED) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                result = new RegistrationResult(true, response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
            HttpURLConnection connection = createConnection(path, "PATCH", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
            HttpURLConnection connection = createConnection(path, "PATCH", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
        int responseCode;
        try {
            String json = getJsonFromHabitDto(habitDto);
            HttpURLConnection connection = createConnection(path, "POST", Session.getToken(), json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK || responseCode == HTTP_CREATED) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(new HabitChangeDto(oldHabitDto, newHabitDto));
            HttpURLConnection connection = createConnection(path, "PATCH", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
        int responseCode;
        try {
            String json = objectMapper.writeValueAsString(habitDto);
            HttpURLConnection connection = createConnection(path, "POST", token, json);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
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
        String path = baseUrl + "/habits" + habitDto.getTitle();
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode;
        try {
            HttpURLConnection connection = createConnection(path, "DELETE", token, null);
            responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                ResponseDto response = objectMapper.readValue(connection.getInputStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            } else {
                ResponseDto response = objectMapper.readValue(connection.getErrorStream(), ResponseDto.class);
                System.out.println(response.getMessage());
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return responseCode == HTTP_OK;
    }

    public List<HabitDto> getHabits(String token) {
        String path = baseUrl + "/habits";
        ObjectMapper objectMapper = new ObjectMapper();
        HabitsListDto habitsListDto = null;
        try {
            HttpURLConnection connection = createConnection(path, "GET", token, null);
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK) {
                habitsListDto = objectMapper.readValue(connection.getInputStream(), HabitsListDto.class);
                Set<Habit> habits = habitsListDto.getHabits().stream()
                        .map(HabitMapper.INSTANCE::habitDtoToHabit)
                        .collect(Collectors.toSet());
                Session.setHabits(habits);
            } else {
                System.out.println("Не удалось загрузить привычки");
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return new LinkedList<>();
        }
        return (habitsListDto == null) ? new LinkedList<>() : habitsListDto.getHabits();
    }

}

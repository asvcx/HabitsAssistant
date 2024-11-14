package org.habitsapp.server;

import io.jsonwebtoken.Claims;
import org.assertj.core.api.SoftAssertions;
import org.habitsapp.server.security.JwtService;
import org.habitsapp.server.security.TokenStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class JwtServiceTest {

    private static JwtService jwt;

    @BeforeAll
    public static void init() throws NoSuchAlgorithmException {
        jwt = new JwtService();
    }

    @Test
    @DisplayName("Generate a token, verify it, then extract the claims")
    public void shouldGenerateTokenAndThenVerifyIt() {
        // Generate token
        SoftAssertions assertions = new SoftAssertions();
        Map<String,String> payload = new HashMap<>();
        payload.put("name", "Ruslan");
        String token = jwt.generateJwt(payload, "test", "123");

        // Check token status
        TokenStatus tokenStatus = jwt.getTokenStatus(token);
        assertions.assertThat(tokenStatus).as("Check token").isEqualTo(TokenStatus.ACTIVE);

        // Check wrong token status
        TokenStatus wrongStatus = jwt.getTokenStatus(token + "1");
        assertions.assertThat(wrongStatus).as("Check wrong token").isEqualTo(TokenStatus.INCORRECT);

        // Extract claims
        Claims claims = jwt.extractClaims(token);
        String name = (String) claims.get("name");
        assertions.assertThat(name).as("Check name").isEqualTo("Ruslan");

        // Check assertions
        assertions.assertAll();
    }

}

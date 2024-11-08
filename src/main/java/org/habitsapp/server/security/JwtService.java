package org.habitsapp.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.*;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {
    @Getter
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public JwtService() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    public String generateJwt(Map<String,String> payload, String subject, String id) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .id(id)
                .issuer("HabitsAssistant")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(privateKey, Jwts.SIG.RS256);
        payload.forEach(builder::claim);
        return builder.compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return null;
        }
    }

    public TokenStatus getTokenStatus(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims == null) {
                return TokenStatus.INCORRECT;
            }
            if (claims.getExpiration().before(new Date())) {
                return TokenStatus.EXPIRED;
            } else {
                return TokenStatus.ACTIVE;
            }
        } catch (ExpiredJwtException e) {
            return TokenStatus.EXPIRED;
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return TokenStatus.INCORRECT;
        }
    }

}

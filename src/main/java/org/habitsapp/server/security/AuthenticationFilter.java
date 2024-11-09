package org.habitsapp.server.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.endsWith("/api/login") || uri.endsWith("/api/profile/create")) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearer = request.getHeader("Authorization");
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = bearer.substring(7);
        TokenStatus status = jwt.getTokenStatus(token);
        if (status == TokenStatus.EXPIRED || status == TokenStatus.INCORRECT) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            Claims claims = jwt.extractClaims(token);
            request.setAttribute("id", claims.getId());
            request.setAttribute("email", claims.get("email"));
            request.setAttribute("access", claims.get("access"));
            System.out.printf("id = %s; email = %s; access = %s%n", claims.getId(), claims.get("email"), claims.get("access"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

}

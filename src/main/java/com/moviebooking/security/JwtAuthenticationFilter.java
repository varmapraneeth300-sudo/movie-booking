package com.moviebooking.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // No Authorization header
        if (authHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrong Authorization format
        if (!authHeader.startsWith("Bearer ")) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid Authorization header"
            );

            return;
        }

        String token = authHeader.substring(7);

        try {

            String username =
                    jwtService.extractUsername(token);

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token has expired"
            );

            return;

        } catch (JwtException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT token"
            );

            return;

        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication failed"
            );

            return;
        }

        // IMPORTANT:
        // Keep this OUTSIDE the try-catch.
        filterChain.doFilter(request, response);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        String json = String.format(
                "{\"status\":%d,\"message\":\"%s\"}",
                status,
                message
        );

        response.getWriter().write(json);
    }
}
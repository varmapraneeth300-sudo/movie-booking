package com.moviebooking.service;

import com.moviebooking.dto.*;
import com.moviebooking.entity.User;
import com.moviebooking.entity.enums.Role;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.UserRepo;
import com.moviebooking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            throw new ApplicationException(
                    "Username already exists",
                    HttpStatus.CONFLICT
            );
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new ApplicationException(
                    "Email already exists",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.USER);

        User savedUser = userRepo.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApplicationException(
                        "Invalid username or password",
                        HttpStatus.UNAUTHORIZED
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ApplicationException(
                    "Invalid username or password",
                    HttpStatus.UNAUTHORIZED
            );
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

    public AuthMeResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(
                        "User not found",
                        HttpStatus.NOT_FOUND
                ));

        return new AuthMeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
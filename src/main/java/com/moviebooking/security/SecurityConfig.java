package com.moviebooking.security;

import com.moviebooking.entity.User;
import com.moviebooking.entity.enums.Role;
import com.moviebooking.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final UserRepo userRepo;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

                        // Login / Register
                        .requestMatchers("/api/auth/**", "/api/webhooks/**")
                        .permitAll()

                        // Public GET APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/movies/**",
                                "/api/theatres/**",
                                "/api/screens/**",
                                "/api/shows/**"
                        )
                        .permitAll()

                        // Admin APIs
                        .requestMatchers(
                                "/api/movies/**",
                                "/api/theatres/**",
                                "/api/screens/**",
                                "/api/shows/**"
                        )
                        .hasRole("ADMIN")

                        // User APIs
                        .requestMatchers("/api/bookings/**")
                        .hasRole("USER")

                        // Authenticated users
                        .requestMatchers("/api/users/**")
                        .authenticated()

                        .requestMatchers("/error").permitAll()

                        .anyRequest()
                        .authenticated()
                )


                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    CommandLineRunner createAdmin(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepo.existsByUsername("admin")) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setEmail("digitalwalletsupporthelp@gmail.com");
                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );
                admin.setRole(Role.ADMIN);

                userRepo.save(admin);
            }
        };
    }
}
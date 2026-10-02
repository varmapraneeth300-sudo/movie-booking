package com.moviebooking.dto;

import com.moviebooking.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String username;
    private Role role;
    private String token;
}
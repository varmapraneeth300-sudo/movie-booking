package com.moviebooking.dto;

import com.moviebooking.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMeResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
}
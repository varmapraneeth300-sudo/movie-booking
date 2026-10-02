package com.moviebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScreenRequest {

    @NotBlank(message = "Screen name is required")
    @Size(max = 100, message = "Screen name must not exceed 100 characters")
    private String name;
}
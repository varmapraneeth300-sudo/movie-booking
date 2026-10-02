package com.moviebooking.dto;

import com.moviebooking.entity.enums.Genre;
import com.moviebooking.entity.enums.Language;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class MovieRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    @Max(value = 600, message = "Duration must not exceed 600 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Language is required")
    private Language language;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotEmpty(message = "At least one genre is required")
    private Set<Genre> genres;
}
package com.moviebooking.dto;

import com.moviebooking.entity.enums.Genre;
import com.moviebooking.entity.enums.Language;
import com.moviebooking.enums.MovieStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class MovieResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private Language language;
    private LocalDate releaseDate;
    private MovieStatus movieStatus;
    private Set<Genre> genres;
}

package com.moviebooking.dto;

import com.moviebooking.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteMovieResponse {

    private Long movieId;
    private MovieStatus status;
    private int cancelledShows;
    private int cancelledBookings;
    private String message;
}
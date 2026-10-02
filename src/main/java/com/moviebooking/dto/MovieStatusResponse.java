package com.moviebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieStatusResponse {

    private Long movieId;
    private String status;
    private String message;
}
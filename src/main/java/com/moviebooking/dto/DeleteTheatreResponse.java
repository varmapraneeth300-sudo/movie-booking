package com.moviebooking.dto;

import com.moviebooking.entity.enums.TheatreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteTheatreResponse {

    private Long theatreId;
    private TheatreStatus status;
    private int deactivatedScreens;
    private int cancelledShows;
    private int cancelledBookings;
    private String message;
}
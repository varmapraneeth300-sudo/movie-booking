package com.moviebooking.dto;


import com.moviebooking.entity.enums.ScreenStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteScreenResponse {

    private Long screenId;
    private ScreenStatus status;
    private int cancelledShows;
    private int cancelledBookings;
    private String message;
}

package com.moviebooking.dto;

import com.moviebooking.entity.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DeleteShowResponse {

    private Long showId;
    private ShowStatus showStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int cancelledBookings;
    private String message;
}
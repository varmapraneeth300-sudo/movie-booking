package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatStatusResponse {

    private Long seatId;
    private SeatStatus status;
    private String message;
}
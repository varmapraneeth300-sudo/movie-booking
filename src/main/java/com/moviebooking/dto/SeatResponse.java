package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatStatus;
import com.moviebooking.entity.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Long screenId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatStatus seatStatus;
    private SeatType seatType;
}
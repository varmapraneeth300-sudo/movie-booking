package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatType;
import com.moviebooking.entity.enums.ShowSeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShowSeatResponse {
    private Long id;
    private Long showId;
    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatType seatType;
    private Integer price;
    private ShowSeatStatus status;
}
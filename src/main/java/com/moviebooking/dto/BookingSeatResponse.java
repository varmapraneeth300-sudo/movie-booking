package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BookingSeatResponse {

    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatType seatType;
    private BigDecimal price;
}
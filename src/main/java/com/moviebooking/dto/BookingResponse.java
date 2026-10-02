package com.moviebooking.dto;

import com.moviebooking.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long showId;
    private BigDecimal totalAmount;
    private BookingStatus status;
}

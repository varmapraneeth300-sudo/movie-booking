package com.moviebooking.dto;

import com.moviebooking.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetBookingResponse {

    private Long bookingId;

    private Long showId;

    private LocalDateTime showStartTime;

    private LocalDateTime showEndTime;

    private BigDecimal totalAmount;

    private BookingStatus status;

    private List<BookingSeatResponse> seats;
}
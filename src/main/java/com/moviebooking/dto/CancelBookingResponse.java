package com.moviebooking.dto;

import com.moviebooking.entity.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelBookingResponse {

    private Long bookingId;
    private BookingStatus status;
    private String message;
}

package com.moviebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingConfirmationResponse {

    private Long bookingId;
    private String razorpayOrderId;
    private String message;
}

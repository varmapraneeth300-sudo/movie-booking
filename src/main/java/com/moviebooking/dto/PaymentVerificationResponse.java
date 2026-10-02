package com.moviebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentVerificationResponse {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String message;
}
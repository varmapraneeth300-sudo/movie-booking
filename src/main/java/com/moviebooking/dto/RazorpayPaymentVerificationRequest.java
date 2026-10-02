package com.moviebooking.dto;

import lombok.Data;

@Data
public class RazorpayPaymentVerificationRequest {
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;
}
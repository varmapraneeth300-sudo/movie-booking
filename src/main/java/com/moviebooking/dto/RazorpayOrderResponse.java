package com.moviebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RazorpayOrderResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
}

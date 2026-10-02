package com.moviebooking.controller;

import com.moviebooking.dto.*;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.RazorpayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RazorpayService razorpayService;

    @PostMapping("/initiate")
    public RazorpayOrderResponse initiateBooking(
            @Valid @RequestBody BookingRequest bookingRequest) {

        return bookingService.initiateBooking(bookingRequest);
    }

    @PostMapping("/payment/verification")
    public BookingConfirmationResponse verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        razorpayService.verifyPayment(request);
        if(razorpayService.checkPaymentStatus(request.getRazorpayOrderId())) {
            return bookingService.finalizeBooking(
                    request.getRazorpayOrderId()
            );
        } else{
            return bookingService.releaseSeatsAfterPaymentFailure(request.getRazorpayOrderId());
        }
    }

    @GetMapping("/{bookingId}")
    public GetBookingResponse getBookingById(
            @PathVariable Long bookingId) {

        return bookingService.getBookingById(bookingId);
    }

    @PostMapping("{bookingId}/cancel")
    public CancelBookingResponse deleteBookingById(
            @PathVariable Long bookingId) {

        return bookingService.cancelBooking(bookingId);
    }
}
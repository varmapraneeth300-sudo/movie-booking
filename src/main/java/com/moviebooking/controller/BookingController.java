package com.moviebooking.controller;

import com.moviebooking.dto.*;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.PaymentService;
import com.moviebooking.service.RazorpayService;
import com.moviebooking.service.ReleaseSeatRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RazorpayService razorpayService;
    private final PaymentService paymentService;

    @PostMapping("/hold")
    public void lockSeat(@RequestBody LockSeatRequestDto lockSeatRequest) {
        bookingService.holdSeat(lockSeatRequest);
    }

    @PostMapping("/initiate")
    public RazorpayOrderResponse initiateBooking(
            @Valid @RequestBody BookingRequest bookingRequest) {

        return bookingService.initiateBooking(bookingRequest);
    }

    @PostMapping("/payment/verification")
    public BookingConfirmationResponse verifyPayment(
        @Valid @RequestBody PaymentVerificationRequest request) {
        razorpayService.verifyPayment(request);
        return paymentService.processVerifiedPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId()
        );
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

    @DeleteMapping("/hold")
    public void releaseSeat(@RequestBody ReleaseSeatRequestDto releaseSeatRequest) {
        bookingService.releaseSeat(releaseSeatRequest);
    }
}
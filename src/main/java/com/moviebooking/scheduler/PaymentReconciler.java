
package com.moviebooking.scheduler;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.BookingSeat;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.enums.BookingStatus;
import com.moviebooking.repo.BookingRepo;
import com.moviebooking.repo.BookingSeatRepo;
import com.moviebooking.service.BookingService;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciler {

    private final BookingRepo bookingRepo;
    private final BookingSeatRepo bookingSeatRepo;
    private final RazorpayClient razorpayClient;
    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60000)
    public void reconcilePayments() {

        List<Booking> bookings = bookingRepo.findByStatusIn(
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.PAYMENT_UNRESOLVED
                )
        );

        for (Booking booking : bookings) {

            try {
                String orderId = booking.getRazorpayOrderId();

                Order order = razorpayClient.orders.fetch(orderId);
                String orderStatus = order.get("status");

                boolean unresolved =
                        booking.getStatus() ==
                                BookingStatus.PAYMENT_UNRESOLVED;

                if ("created".equals(orderStatus)) {

                    if (!unresolved && isPaymentInitiationExpired(booking)) {
                        bookingService.expireBooking(booking.getId());
                    }

                } else if ("attempted".equals(orderStatus)) {

                    List<Payment> payments =
                            razorpayClient.orders.fetchPayments(orderId);

                    boolean captured = false;
                    boolean paymentInProgress = false;
                    boolean allPaymentsFailed = !payments.isEmpty();

                    for (Payment payment : payments) {

                        String paymentStatus = payment.get("status");

                        if ("captured".equals(paymentStatus)) {
                            captured = true;
                            break;
                        }

                        if ("created".equals(paymentStatus)
                                || "authorized".equals(paymentStatus)) {
                            paymentInProgress = true;
                        }

                        if (!"failed".equals(paymentStatus)) {
                            allPaymentsFailed = false;
                        }
                    }

                    if (captured) {

                        if (unresolved) {
                            bookingService.markRefundRequired(booking.getId());
                        } else {
                            bookingService.confirmBooking(booking.getId());
                        }

                    } else if (allPaymentsFailed) {

                        if (unresolved) {
                            bookingService.markUnresolvedPaymentFailed(booking.getId());
                        } else if (isHoldExpired(booking)) {
                            bookingService.expireBooking(booking.getId());
                        }

                    } else if (paymentInProgress) {

                        if (!unresolved && isHoldExpired(booking)) {
                            bookingService.markPaymentUnresolved(
                                    booking.getId()
                            );
                        }

                        // If already unresolved, keep checking Razorpay.
                    }

                } else if ("paid".equals(orderStatus)) {

                    if (unresolved) {
                        bookingService.markRefundRequired(booking.getId());
                    } else {
                        bookingService.confirmBooking(booking.getId());
                    }
                }

            } catch (RazorpayException e) {

                log.error(
                        "Razorpay reconciliation failed for booking {}",
                        booking.getId(),
                        e
                );
            }
        }
    }

    private boolean isPaymentInitiationExpired(Booking booking) {

        return booking.getPaymentInitiationExpiresAt() != null
                && !booking.getPaymentInitiationExpiresAt()
                .isAfter(LocalDateTime.now());
    }

    private boolean isHoldExpired(Booking booking) {

        List<BookingSeat> bookingSeats =
                bookingSeatRepo.findByBooking(booking);

        return !bookingSeats.isEmpty()
                && bookingSeats.stream().allMatch(bs -> {

            ShowSeat seat = bs.getShowSeat();

            return seat.getHoldExpiresAt() != null
                    && !seat.getHoldExpiresAt()
                    .isAfter(LocalDateTime.now());
        });
    }
}

package com.moviebooking.service;

import com.moviebooking.dto.BookingConfirmationResponse;
import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Payment;
import com.moviebooking.entity.enums.PaymentProvider;
import com.moviebooking.entity.enums.PaymentStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.BookingRepo;
import com.moviebooking.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepo paymentRepo;
    private final BookingRepo bookingRepo;
    private final BookingService bookingService;
    private final RazorpayService razorpayService;

    @Transactional
    public BookingConfirmationResponse processVerifiedPayment(
            String orderId,
            String paymentId) {
        com.razorpay.Payment razorpayPayment = razorpayService.fetchPayment(paymentId);

        if (!orderId.equals(razorpayPayment.get("order_id"))) {
            throw new ApplicationException(
                    "Payment does not belong to this order",
                    HttpStatus.BAD_REQUEST
            );
        }

        Object amountValue = razorpayPayment.get("amount");
        if (!(amountValue instanceof Number amountInPaise)) {
            throw new ApplicationException(
                    "Razorpay payment is missing its amount",
                    HttpStatus.BAD_GATEWAY
            );
        }

        Payment payment = recordPayment(
                orderId,
                paymentId,
                amountInPaise.longValue(),
                toPaymentStatus(razorpayPayment.get("status")),
                razorpayPayment.get("error_description")
        );

        return applyPaymentStatus(payment);
    }

    @Transactional
    public void processReconciledPayment(
            String orderId,
            com.razorpay.Payment razorpayPayment) {
        if (!orderId.equals(razorpayPayment.get("order_id"))) {
            throw new ApplicationException(
                    "Payment does not belong to this order",
                    HttpStatus.BAD_REQUEST
            );
        }

        Object amountValue = razorpayPayment.get("amount");
        if (!(amountValue instanceof Number amountInPaise)) {
            throw new ApplicationException(
                    "Razorpay payment is missing its amount",
                    HttpStatus.BAD_GATEWAY
            );
        }

        Payment payment = recordPayment(
                orderId,
                razorpayPayment.get("id"),
                amountInPaise.longValue(),
                toPaymentStatus(razorpayPayment.get("status")),
                razorpayPayment.get("error_description")
        );

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            applyPaymentStatus(payment);
        }
    }

    @Transactional
    public void processWebhook(String payload, String signature) {
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            throw new ApplicationException(
                    "Invalid Razorpay webhook signature",
                    HttpStatus.BAD_REQUEST
            );
        }

        JSONObject event;
        try {
            event = new JSONObject(payload);
        } catch (JSONException exception) {
            throw new ApplicationException(
                    "Invalid Razorpay webhook payload",
                    HttpStatus.BAD_REQUEST
            );
        }

        String eventName = event.optString("event", "");
        if ("refund.processed".equals(eventName)) {
            processRefundEvent(event);
            return;
        }

        if (!"payment.captured".equals(eventName)
                && !"payment.failed".equals(eventName)
                && !"payment.authorized".equals(eventName)) {
            log.info("Ignoring unsupported Razorpay webhook event: {}", eventName);
            return;
        }

        JSONObject paymentEntity = getEventEntity(event, "payment");
        String orderId = paymentEntity.optString("order_id", "");
        String paymentId = paymentEntity.optString("id", "");
        if (orderId.isBlank() || paymentId.isBlank()
                || !paymentEntity.has("amount")) {
            throw new ApplicationException(
                    "Razorpay webhook is missing payment details",
                    HttpStatus.BAD_REQUEST
            );
        }

        PaymentStatus status = switch (eventName) {
            case "payment.captured" -> PaymentStatus.SUCCESS;
            case "payment.failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };

        Payment payment = recordPayment(
                orderId,
                paymentId,
                paymentEntity.getLong("amount"),
                status,
                paymentEntity.optString("error_description", null)
        );
        applyPaymentStatus(payment);
    }

    private Payment recordPayment(
            String orderId,
            String paymentId,
            long amountInPaise,
            PaymentStatus incomingStatus,
            String failureReason) {
        Booking booking = bookingRepo.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ApplicationException(
                        "Booking not found for Razorpay order",
                        HttpStatus.NOT_FOUND
                ));

        BigDecimal amount = BigDecimal.valueOf(amountInPaise, 2);
        if (booking.getTotalAmount().compareTo(amount) != 0) {
            throw new ApplicationException(
                    "Razorpay payment amount does not match the booking amount",
                    HttpStatus.CONFLICT
            );
        }

        Payment payment = paymentRepo.findByProviderPaymentId(paymentId)
                .orElseGet(Payment::new);

        if (payment.getBooking() != null
                && !payment.getBooking().getId().equals(booking.getId())) {
            throw new ApplicationException(
                    "Razorpay payment is already linked to another booking",
                    HttpStatus.CONFLICT
            );
        }

        PaymentStatus currentStatus = payment.getStatus();
        if (currentStatus == PaymentStatus.SUCCESS
                || currentStatus == PaymentStatus.REFUNDED
                || currentStatus == PaymentStatus.PARTIALLY_REFUNDED) {
            if (incomingStatus == PaymentStatus.FAILED
                    || incomingStatus == PaymentStatus.PENDING) {
                return payment;
            }
        }

        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setProvider(PaymentProvider.RAZORPAY);
        payment.setProviderOrderId(orderId);
        payment.setProviderPaymentId(paymentId);
        payment.setStatus(incomingStatus);
        payment.setFailureReason(failureReason);
        return paymentRepo.save(payment);
    }

    private BookingConfirmationResponse applyPaymentStatus(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return bookingService.handleCapturedPayment(
                    payment.getProviderOrderId()
            );
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return bookingService.releaseSeatsAfterPaymentFailure(
                    payment.getProviderOrderId()
            );
        }

        Booking booking = payment.getBooking();
        return new BookingConfirmationResponse(
                booking.getId(),
                payment.getProviderOrderId(),
                "Payment is not captured yet. Booking remains pending."
        );
    }

    private void processRefundEvent(JSONObject event) {
        JSONObject refundEntity = getEventEntity(event, "refund");
        String paymentId = refundEntity.optString("payment_id", "");
        if (paymentId.isBlank() || !refundEntity.has("amount")) {
            throw new ApplicationException(
                    "Razorpay refund webhook is missing refund details",
                    HttpStatus.BAD_REQUEST
            );
        }

        Payment payment = paymentRepo.findByProviderPaymentId(paymentId)
                .orElseThrow(() -> new ApplicationException(
                        "Payment not found for Razorpay refund",
                        HttpStatus.NOT_FOUND
                ));
        BigDecimal refundAmount = BigDecimal.valueOf(
                refundEntity.getLong("amount"),
                2
        );
        payment.setStatus(refundAmount.compareTo(payment.getAmount()) >= 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED);
        paymentRepo.save(payment);
    }

    private JSONObject getEventEntity(JSONObject event, String resourceName) {
        try {
            return event.getJSONObject("payload")
                    .getJSONObject(resourceName)
                    .getJSONObject("entity");
        } catch (JSONException exception) {
            throw new ApplicationException(
                    "Invalid Razorpay webhook payload",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private PaymentStatus toPaymentStatus(String razorpayStatus) {
        return switch (razorpayStatus) {
            case "captured" -> PaymentStatus.SUCCESS;
            case "failed" -> PaymentStatus.FAILED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}

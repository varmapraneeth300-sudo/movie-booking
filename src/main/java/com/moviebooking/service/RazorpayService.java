package com.moviebooking.service;

import com.moviebooking.dto.PaymentVerificationRequest;
import com.moviebooking.dto.PaymentVerificationResponse;
import com.moviebooking.dto.RazorpayOrderResponse;
import com.moviebooking.exception.ApplicationException;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;


    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;


    public RazorpayService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    public RazorpayOrderResponse createOrder(BigDecimal amountInRupees, LocalDateTime orderExpiry)
            throws Exception {

        long amountInPaise = amountInRupees
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put(
                "receipt",
                "wallet_deposit_" + System.currentTimeMillis()
        );

        Order order = razorpayClient.orders.create(orderRequest);

        return new RazorpayOrderResponse(
                order.get("id"),
                amountInRupees,
                order.get("currency"),
                orderExpiry
        );
    }

    public PaymentVerificationResponse verifyPayment(
            PaymentVerificationRequest request) {

        System.out.println("entered payment verification");

        try {

            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId()
            );

            attributes.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId()
            );

            attributes.put(
                    "razorpay_signature",
                    request.getRazorpaySignature()
            );

            boolean valid =
                    Utils.verifyPaymentSignature(
                            attributes,
                            keySecret
                    );

            if (!valid) {
                throw new ApplicationException(
                        "Payment verification failed",
                        HttpStatus.BAD_REQUEST
                );
            }

            return new PaymentVerificationResponse(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    "Payment verified successfully"
            );

        } catch (ApplicationException e) {
            throw e;

        } catch (Exception e) {
            throw new ApplicationException(
                    "Unable to verify payment",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    public boolean checkPaymentStatus(String razorpayOrderId) {
        try{
            List<Payment> payments =
                    razorpayClient.orders.fetchPayments(razorpayOrderId);
            for (Payment payment : payments) {

                String status = payment.get("status");

                if ("captured".equals(status)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new ApplicationException("Razorpay exception", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void refundCapturedPayment(String razorpayOrderId) {
        try {
            List<Payment> payments =
                    razorpayClient.orders.fetchPayments(razorpayOrderId);

            Payment capturedPayment = payments.stream()
                    .filter(payment -> "captured".equals(payment.get("status")))
                    .findFirst()
                    .orElseThrow(() -> new ApplicationException(
                            "No captured payment found for this booking",
                            HttpStatus.CONFLICT
                    ));

            String paymentId = capturedPayment.get("id");
            razorpayClient.payments.refund(paymentId);
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApplicationException(
                    "Unable to initiate payment refund",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new ApplicationException(
                    "Razorpay webhook secret is not configured",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        try {
            return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
        } catch (Exception exception) {
            throw new ApplicationException(
                    "Unable to verify Razorpay webhook signature",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public com.razorpay.Payment fetchPayment(String paymentId) {
        try {
            return razorpayClient.payments.fetch(paymentId);
        } catch (Exception exception) {
            throw new ApplicationException(
                    "Unable to retrieve Razorpay payment",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}
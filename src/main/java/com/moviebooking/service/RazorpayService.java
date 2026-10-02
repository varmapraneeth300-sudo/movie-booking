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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;


    @Value("${razorpay.key.secret}")
    private String keySecret;


    public RazorpayService(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    public RazorpayOrderResponse createOrder(BigDecimal amountInRupees)
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
                order.get("currency")
        );
    }

    public PaymentVerificationResponse verifyPayment(
            PaymentVerificationRequest request) {

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


    private String generateSignature(
            String payload,
            String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey =
                new SecretKeySpec(
                        secret.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                );

        mac.init(secretKey);

        byte[] hash =
                mac.doFinal(
                        payload.getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {

            String hex =
                    Integer.toHexString(
                            0xff & b
                    );

            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();
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
}
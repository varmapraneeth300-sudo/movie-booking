package com.moviebooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
public class RazorpayWebhookController {

    @PostMapping("/razorpay")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload) {

        System.out.println("========== RAZORPAY WEBHOOK ==========");
        System.out.println(payload);

        return ResponseEntity.ok().build();
    }
}

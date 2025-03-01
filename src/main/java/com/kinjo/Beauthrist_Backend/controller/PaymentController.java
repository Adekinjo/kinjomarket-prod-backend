package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.PaymentRequest;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.service.interf.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initialize")
    public Response initializePayment(@RequestBody PaymentRequest request) {
        return paymentService.initializePayment(request);
    }

    @GetMapping("/verify")
    public Response verifyPayment(@RequestParam String transactionId) {
        System.out.println("Verifying payment with transactionId: " + transactionId); // Debugging
        return paymentService.verifyPayment(transactionId);
    }
}


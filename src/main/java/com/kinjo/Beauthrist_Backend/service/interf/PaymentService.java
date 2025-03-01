package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.PaymentRequest;
import com.kinjo.Beauthrist_Backend.dto.Response;

public interface PaymentService {
    Response initializePayment(PaymentRequest request);
    Response verifyPayment(String transactionId);
}


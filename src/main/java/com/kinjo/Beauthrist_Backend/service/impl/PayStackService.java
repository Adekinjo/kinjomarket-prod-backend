package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.entity.Order;
import com.kinjo.Beauthrist_Backend.entity.OrderItem;
import com.kinjo.Beauthrist_Backend.entity.Payment;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.enums.OrderStatus;
import com.kinjo.Beauthrist_Backend.enums.PaymentStatus;
import com.kinjo.Beauthrist_Backend.repository.OrderRepo;
import com.kinjo.Beauthrist_Backend.repository.PaymentRepo;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.math.BigDecimal;

@Service
public class PayStackService {

    private OrderRepo orderRepo;
    private PaymentRepo paymentRepo;

    @Value("${paystack.secret.key}")
    private String payStackSecretKey;

    private static final String INITIALIZE_PAYMENT_URL = "https://api.paystack.co/transaction/initialize";
    private static final String VERIFY_PAYMENT_URL = "https://api.paystack.co/transaction/verify/";

    public String initializePayment(BigDecimal amount, String email, String currency) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(INITIALIZE_PAYMENT_URL);
            request.setHeader("Authorization", "Bearer " + payStackSecretKey);
            request.setHeader("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            json.put("amount", amount.multiply(new BigDecimal(100)));
            json.put("email", email);
            json.put("currency", currency);
            System.out.println("PayStack API request payload: " + json.toString());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            String response = EntityUtils.toString(httpClient.execute(request).getEntity());
            JSONObject responseJson = new JSONObject(response);
            System.out.println("PayStack API response: " + responseJson.toString()); // Debugging

            if (!responseJson.getBoolean("status")) {
                throw new RuntimeException("PayStack API error: " + responseJson.getString("message"));
            }

            // Check if the response contains the authorization URL
            if (!responseJson.has("data") || !responseJson.getJSONObject("data").has("authorization_url")) {
                throw new RuntimeException("PayStack API error: No authorization URL returned");
            }

            return responseJson.getJSONObject("data").getString("authorization_url");
        }
    }


    public boolean verifyPayment(String transactionId) throws Exception {
        // Validate input
        if (transactionId == null || transactionId.isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be empty");
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the request to Paystack's verify endpoint
            HttpGet request = new HttpGet(VERIFY_PAYMENT_URL + transactionId);
            request.setHeader("Authorization", "Bearer " + payStackSecretKey);

            // Execute the request and get the response
            String response = EntityUtils.toString(httpClient.execute(request).getEntity());
            JSONObject responseJson = new JSONObject(response);
            System.out.println("PayStack API response: " + responseJson.toString()); // Debugging

            // Check if the Paystack API returned a success status
            if (!responseJson.getBoolean("status")) {
                throw new RuntimeException("PayStack API error: " + responseJson.getString("message"));
            }

            // Check if the response contains the payment status
            if (!responseJson.has("data") || !responseJson.getJSONObject("data").has("status")) {
                throw new RuntimeException("PayStack API error: No payment status returned");
            }

            // Check if the payment was successful
            boolean isPaymentSuccessful = responseJson.getJSONObject("data").getString("status").equals("success");

            if (isPaymentSuccessful) {
                // Fetch the payment from the database
                Payment payment = paymentRepo.findByTransactionId(transactionId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));

                // Update the payment status to SUCCESS
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepo.save(payment);

                // Update the associated order status to PAID
                Order order = payment.getOrder();
                order.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepo.save(order);
            }

            return isPaymentSuccessful;
        }
    }

}
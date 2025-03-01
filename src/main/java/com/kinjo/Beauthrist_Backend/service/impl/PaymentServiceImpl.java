package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.PaymentRequest;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.Order;
import com.kinjo.Beauthrist_Backend.entity.OrderItem;
import com.kinjo.Beauthrist_Backend.entity.Payment;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.enums.OrderStatus;
import com.kinjo.Beauthrist_Backend.enums.PaymentStatus;
import com.kinjo.Beauthrist_Backend.repository.OrderRepo;
import com.kinjo.Beauthrist_Backend.repository.PaymentRepo;
import com.kinjo.Beauthrist_Backend.repository.UserRepo;
import com.kinjo.Beauthrist_Backend.service.EmailService;
import com.kinjo.Beauthrist_Backend.service.interf.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PayStackService payStackService;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OrderRepo orderRepo;

    private final EmailService emailService;

    @Override
    public Response initializePayment(PaymentRequest request) {
        try {
            // Fetch the order
            Order order = orderRepo.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

            // Fetch the user from the database
            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            // Initialize payment with Paystack
            String authorizationUrl = payStackService.initializePayment(
                    request.getAmount(),
                    request.getEmail(),
                    request.getCurrency()
            );

            // Check if the authorization URL is valid
            if (authorizationUrl == null || authorizationUrl.isEmpty()) {
                throw new RuntimeException("Failed to initialize payment: No authorization URL returned from Paystack");
            }

            // Save payment details to the database
            Payment payment = new Payment();
            payment.setAmount(request.getAmount());
            payment.setEmail(request.getEmail());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setOrder(order);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setUser(user);
            paymentRepo.save(payment);

            return Response.builder()
                    .status(200)
                    .message("Payment initialized successfully")
                    .authorizationUrl(authorizationUrl)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.builder()
                    .status(500)
                    .message("Error initializing payment: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response verifyPayment(String transactionId) {
        try {
            // Verify the payment with Paystack
            boolean isPaymentSuccessful = payStackService.verifyPayment(transactionId);

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

                User user = order.getUser();


                sendCustomerOrderConfirmation(user, order);
                sendAdminOrderNotification(user, order);

                return Response.builder()
                        .status(200)
                        .message("Payment verified successfully")
                        .build();
            } else {
                return Response.builder()
                        .status(400)
                        .message("Payment verification failed")
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.builder()
                    .status(500)
                    .message("Error verifying payment: " + e.getMessage())
                    .build();
        }
    }


    // Method to send confirmation email to the customer
    private void sendCustomerOrderConfirmation(User user, Order order) {
        String subject = "Order Confirmation";
        String body = "Dear " + user.getName() + ",\n\n"
                + "Thank you for your order. Here are your order details:\n\n"
                + "Order ID: " + order.getId() + "\n"
                + "Total Price: NGN" + order.getTotalPrice() + "\n"
                + "Delivery Address: " + user.getAddress().getStreet() + ", "
                + user.getAddress().getCity() + ", "
                + user.getAddress().getState() + " "
                + user.getAddress().getZipcode() + ", "
                + user.getAddress().getCountry() + "\n\n"
                + "You will receive another email once your order has been shipped.\n\n"
                + "Best regards,\n"
                + "The Kinjomarket Team";

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    // Method to send the order notification to the admin
    private void sendAdminOrderNotification(User user, Order order) {
        String adminEmail = "blessingjohn9696@gmail.com"; // Replace with the actual admin email
        String subject = "New Order Placed - Order ID " + order.getId();
        String body = "Hello Admin,\n\n"
                + "A new order has been placed by " + user.getName() + ". Here are the order details:\n\n"
                + "Customer Name: " + user.getName() + "\n"
                + "Customer Email: " + user.getEmail() + "\n"
                + "Order ID: " + order.getId() + "\n"
                + "Total Price: NGN" + order.getTotalPrice() + "\n"
                + "Delivery Address: " + user.getAddress().getStreet() + ", "
                + user.getAddress().getCity() + ", "
                + user.getAddress().getState() + " "
                + user.getAddress().getZipcode() + ", "
                + user.getAddress().getCountry() + "\n\n"
                + "Order Items:\n";

        for (OrderItem orderItem : order.getOrderItemList()) {
            body += "Product: " + orderItem.getProduct().getName() + ", Quantity: "
                    + orderItem.getQuantity() + ", Price: $"
                    + orderItem.getPrice() + "\n";
        }

        body += "\nThank you for reviewing this order.";

        emailService.sendEmail(adminEmail, subject, body);
    }

}
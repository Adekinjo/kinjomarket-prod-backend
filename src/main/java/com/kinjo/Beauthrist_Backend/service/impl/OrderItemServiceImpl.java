
package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.*;
import com.kinjo.Beauthrist_Backend.entity.Order;
import com.kinjo.Beauthrist_Backend.entity.OrderItem;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.enums.OrderStatus;
import com.kinjo.Beauthrist_Backend.enums.UserRole;
import com.kinjo.Beauthrist_Backend.exception.InsufficientStockException;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.OrderItemRepo;
import com.kinjo.Beauthrist_Backend.repository.OrderRepo;
import com.kinjo.Beauthrist_Backend.repository.ProductRepo;
import com.kinjo.Beauthrist_Backend.service.EmailService;
import com.kinjo.Beauthrist_Backend.service.interf.OrderItemService;
import com.kinjo.Beauthrist_Backend.service.interf.PaymentService;
import com.kinjo.Beauthrist_Backend.service.interf.UserService;
import com.kinjo.Beauthrist_Backend.specification.OrderItemSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final EntityDtoMapper entityDtoMapper;
    private final EmailService emailService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public Response placeOrder(OrderRequest orderRequest) {
        try {
            log.info("Starting order placement process for user.");

            // Fetch the logged-in user
            User user = userService.getLoginUser();
            if (user == null) {
                log.error("User not found while placing order.");
                throw new NotFoundException("User not found");
            }

            // Validate order items
            if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
                log.error("Order items are null or empty.");
                throw new IllegalArgumentException("Order items cannot be null or empty");
            }

            // Map order request items to order entities
            List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
                Product product = productRepo.findById(orderItemRequest.getProductId())
                        .orElseThrow(() -> {
                            log.error("Product not found with ID: {}", orderItemRequest.getProductId());
                            return new NotFoundException("Product Not Found");
                        });

                // Check if the product is still in stock
                if (product.getStock() < orderItemRequest.getQuantity()) {
                    log.error("Insufficient stock for product: {}", product.getName());
                    throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
                }

                // Reduce the product's stock by the requested quantity
                product.setStock(product.getStock() - orderItemRequest.getQuantity());
                productRepo.save(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setSize(orderItemRequest.getSize());
                orderItem.setColor(orderItemRequest.getColor());
                orderItem.setProduct(product);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setPrice(product.getNewPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
                orderItem.setOrderStatus(OrderStatus.PENDING);
                orderItem.setUser(user); // Set the user for the order item
                return orderItem;
            }).collect(Collectors.toList());

            // Calculate the total price
            BigDecimal totalPrice = orderRequest.getTotalPrice() != null && orderRequest.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                    ? orderRequest.getTotalPrice()
                    : orderItems.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create the order entity
            Order order = new Order();
            order.setUser(user);
            order.setOrderItemList(orderItems);
            order.setTotalPrice(totalPrice);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());

            // Set the order reference in each order item
            orderItems.forEach(orderItem -> orderItem.setOrder(order));

            // Save the order
            Order savedOrder = orderRepo.save(order);
            log.info("Order saved successfully with ID: {}", savedOrder.getId());

            // Send email notifications
            sendOrderNotifications(savedOrder);

            // Initialize payment if the payment method is Paystack
            if (orderRequest.getPaymentMethod().equals("Paystack")) {
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setAmount(totalPrice);
                paymentRequest.setEmail(user.getEmail());
                paymentRequest.setCurrency("NGN");
                paymentRequest.setUserId(user.getId());
                paymentRequest.setOrderId(savedOrder.getId());

                // Initialize payment
                Response paymentResponse = paymentService.initializePayment(paymentRequest);
                log.info("Payment initialized successfully for order ID: {}", savedOrder.getId());

                return Response.builder()
                        .status(200)
                        .message("Payment initialized successfully")
                        .authorizationUrl(paymentResponse.getAuthorizationUrl())
                        .orderId(savedOrder.getId())
                        .build();
            }

            log.info("Order placed successfully. Order ID: {}", savedOrder.getId());
            return Response.builder()
                    .status(200)
                    .message("Order placed successfully")
                    .orderId(savedOrder.getId())
                    .build();
        } catch (NotFoundException | IllegalArgumentException | InsufficientStockException e) {
            log.error("Error occurred while placing order: {}", e.getMessage(), e);
            throw e; // Re-throw the exception to be handled by the global exception handler
        } catch (Exception e) {
            log.error("Unexpected error occurred while placing order: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again.", e);
        }
    }

    // Method to send email notifications
    private void sendOrderNotifications(Order order) {
        try {
            // Send confirmation email to the user
            sendCustomerOrderConfirmation(order.getUser(), order);

            // Send notification to the admin
            sendAdminOrderNotification(order.getUser(), order);

            // Send notification to the company (if applicable)
            for (OrderItem orderItem : order.getOrderItemList()) {
                User company = orderItem.getProduct().getUser(); // Assuming the product has a reference to the company/user who added it
                if (company != null && company.getUserRole() == UserRole.ROLE_COMPANY) {
                    sendCompanyOrderNotification(company, order, orderItem);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send email notifications: {}", e.getMessage(), e);
        }
    }

    // Method to send confirmation email to the customer
    private void sendCustomerOrderConfirmation(User user, Order order) {
        try {
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
            log.info("Order confirmation email sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to user: {}", user.getEmail(), e);
        }
    }

    // Method to send the order notification to the admin
    private void sendAdminOrderNotification(User user, Order order) {
        try {
            String adminEmail = "kinjomarketmessage@gmail.com";
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
                        + orderItem.getQuantity() + ", Price: NGN"
                        + orderItem.getPrice() + "\n";
            }

            body += "\nThank you for reviewing this order.";

            emailService.sendEmail(adminEmail, subject, body);
            log.info("Order notification email sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send order notification email to admin: {}", e.getMessage());
        }
    }

    // Method to send the order notification to the company
    private void sendCompanyOrderNotification(User company, Order order, OrderItem orderItem) {
        try {
            String subject = "New Order for Your Product - Order ID " + order.getId();
            String body = "Hello " + company.getName() + ",\n\n"
                    + "A new order has been placed for your product. Here are the order details:\n\n"
                    + "Customer Name: " + order.getUser().getName() + "\n"
                    + "Customer Email: " + order.getUser().getEmail() + "\n"
                    + "Order ID: " + order.getId() + "\n"
                    + "Product: " + orderItem.getProduct().getName() + "\n"
                    + "Quantity: " + orderItem.getQuantity() + "\n"
                    + "Price: NGN" + orderItem.getPrice() + "\n"
                    + "Total Order Price: NGN" + order.getTotalPrice() + "\n\n"
                    + "Please prepare the product for shipping.\n\n"
                    + "Best regards,\n"
                    + "The Kinjomarket Team";

            emailService.sendEmail(company.getEmail(), subject, body);
            log.info("Order notification email sent to company: {}", company.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order notification email to company: {}", company.getEmail(), e);
        }
    }

    @Override
    public Response updateOrderItemStatus(Long orderItemId, String status) {
        try {
            OrderItem orderItem = orderItemRepo.findById(orderItemId)
                    .orElseThrow(() -> {
                        log.error("Order item not found with ID: {}", orderItemId);
                        return new NotFoundException("Order Item not found");
                    });

            orderItem.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
            orderItemRepo.save(orderItem);
            log.info("Order item status updated successfully for ID: {}", orderItemId);

            return Response.builder()
                    .status(200)
                    .message("Order status updated successfully")
                    .build();
        } catch (NotFoundException e) {
            log.error("Error updating order item status: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating order item status: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again.", e);
        }
    }

    @Override
    public Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable) {
        try {
            Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasStatus(status))
                    .and(OrderItemSpecification.createdBetween(startDate, endDate))
                    .and(OrderItemSpecification.hasItemId(itemId));

            Page<OrderItem> orderItemPage = orderItemRepo.findAll(spec, pageable);

            if (orderItemPage.isEmpty()) {
                log.error("No orders found with the specified filters.");
                throw new NotFoundException("No Order Found");
            }

            List<OrderItemDto> orderItemDtos = orderItemPage.getContent().stream()
                    .map(entityDtoMapper::mapOrderItemToDtoPlusProductAndUser)
                    .collect(Collectors.toList());

            return Response.builder()
                    .status(200)
                    .orderItemList(orderItemDtos)
                    .totalPage(orderItemPage.getTotalPages())
                    .totalElement(orderItemPage.getTotalElements())
                    .build();
        } catch (NotFoundException e) {
            log.error("Error filtering order items: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error filtering order items: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again.", e);
        }
    }

    @Override
    public Response getCompanyProductOrders(Long companyId, Pageable pageable) {
        try {
            // Fetch all products belonging to the company
            List<Long> productIds = productRepo.findByUserId(companyId).stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (productIds.isEmpty()) {
                log.error("No products found for company with ID: {}", companyId);
                throw new NotFoundException("No products found for the company");
            }

            // Fetch order items for the products
            Page<OrderItem> orderItemsPage = orderItemRepo.findByProductIdIn(productIds, pageable);

            if (orderItemsPage.isEmpty()) {
                log.error("No orders found for company's products with ID: {}", companyId);
                throw new NotFoundException("No orders found for the company's products");
            }

            // Map order items to DTOs
            List<OrderItemDto> orderItemDtos = orderItemsPage.getContent().stream()
                    .map(entityDtoMapper::mapOrderItemToDtoPlusProductAndUser)
                    .collect(Collectors.toList());

            return Response.builder()
                    .status(200)
                    .orderItemList(orderItemDtos)
                    .totalPage(orderItemsPage.getTotalPages())
                    .totalElement(orderItemsPage.getTotalElements())
                    .build();
        } catch (NotFoundException e) {
            log.error("Error fetching company product orders: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching company product orders: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred. Please try again.", e);
        }
    }
}

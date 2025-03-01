package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.*;
import com.kinjo.Beauthrist_Backend.entity.Order;
import com.kinjo.Beauthrist_Backend.entity.OrderItem;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.enums.OrderStatus;
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
            // Fetch the logged-in user
            User user = userService.getLoginUser();
            if (user == null) {
                throw new NotFoundException("User not found");
            }

            // Map order request items to order entities
            List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
                Product product = productRepo.findById(orderItemRequest.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product Not Found"));

                // Check if the product is still in stock
                if (product.getStock() < orderItemRequest.getQuantity()) {
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
                return Response.builder()
                        .status(200)
                        .message("Payment initialized successfully")
                        .authorizationUrl(paymentResponse.getAuthorizationUrl())
                        .orderId(savedOrder.getId())
                        .build();
            }

            return Response.builder()
                    .status(200)
                    .message("Order placed successfully")
                    .orderId(savedOrder.getId())
                    .build();
        } catch (NotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred. Please try again.", e);
        }
    }

    @Override
    public Response updateOrderItemStatus(Long orderItemId, String status) {
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(()-> new NotFoundException("Order Item not found"));

        orderItem.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
        orderItemRepo.save(orderItem);
        return Response.builder()
                .status(200)
                .message("Order status updated successfully")
                .build();
    }

    @Override
    public Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable) {
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasStatus(status))
                .and(OrderItemSpecification.createdBetween(startDate, endDate))
                .and(OrderItemSpecification.hasItemId(itemId));

        Page<OrderItem> orderItemPage = orderItemRepo.findAll(spec, pageable);

        if (orderItemPage.isEmpty()){
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
    }

    @Override
    public Response getCompanyProductOrders(Long companyId, Pageable pageable) {
        // Fetch all products belonging to the company
        List<Long> productIds = productRepo.findByUserId(companyId).stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            throw new NotFoundException("No products found for the company");
        }

        // Fetch order items for the products
        Page<OrderItem> orderItemsPage = orderItemRepo.findByProductIdIn(productIds, pageable);

        if (orderItemsPage.isEmpty()) {
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
    }


}

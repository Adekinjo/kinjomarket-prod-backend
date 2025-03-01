package com.kinjo.Beauthrist_Backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private List<SubCategoryDto> subCategoryList;
    private SubCategoryDto subCategory;


    private int status;
    private String message;

    private  LocalDateTime timeStamp = LocalDateTime.now();

    private String token;
    private String role;
    private String expirationTime;

    private int totalPage;
    private long totalElement;

    private AddressDto address;

    private UserDto user;

    private LocalDateTime createdAt;

    private List<UserDto> userList;



    private CategoryDto category;
    private List<CategoryDto> categoryList;

    private ProductDto product;
    private List<ProductDto> productList;

    private OrderItemDto orderItem;
    private List<OrderItemDto> orderItemList;
    private Long orderId;
    private OrderDto order;
    private List<OrderDto> orderList;

    private BigDecimal totalPrice;
    private String accessToken;
    private String refreshToken;

    public Response(String message, String accessToken) {
        this.message = message;
        this.accessToken = accessToken;
    }

    private String authorizationUrl;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private List<SearchSuggestionDto> suggestions;

    //  deal
    private DealDto deal;
    private List<DealDto> dealList;

    private List<ProductDto> trendingProducts;

    private NewsletterSubscriberDto subscriber; // For single subscriber
    private List<NewsletterSubscriberDto> subscribers;

    private List<ProductDto> likes;

}

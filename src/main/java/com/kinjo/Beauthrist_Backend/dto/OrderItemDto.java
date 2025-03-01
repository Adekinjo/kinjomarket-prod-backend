package com.kinjo.Beauthrist_Backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kinjo.Beauthrist_Backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder  //  new
public class OrderItemDto {

    private Long id;
    private int quantity;
    private BigDecimal price;
    private String status;
    private UserDto user;
    private Long productId;
    private LocalDateTime createdAt;

    private String productName;
    private String productImageUrl;
    private String selectedSize;
    private String selectedColor;

}

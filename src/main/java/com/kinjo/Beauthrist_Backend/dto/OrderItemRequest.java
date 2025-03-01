package com.kinjo.Beauthrist_Backend.dto;

import lombok.Data;


@Data
public class OrderItemRequest {
    private Long productId;
    private int quantity;

    private String size;
    private String color;
}

package com.kinjo.Beauthrist_Backend.dto;

import lombok.Data;

@Data
public class DealRequest {
    private Long productId;
    private int durationHours;
}

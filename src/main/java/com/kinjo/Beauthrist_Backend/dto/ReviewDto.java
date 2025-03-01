package com.kinjo.Beauthrist_Backend.dto;//package com.kinjo.Beauthrist_Backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private int rating;
    private String comment;
    private LocalDateTime timestamp;
    private Long productId; // ID of the product being reviewed
    private Long userId;    // ID of the user who wrote the review
    private String userName;
    // Getters and Setters
}




//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//@Data
//@AllArgsConstructor
//public class ReviewDto {
//    private Long id;
//    private String reviewText;
//    private String reviewerName;
//
//}
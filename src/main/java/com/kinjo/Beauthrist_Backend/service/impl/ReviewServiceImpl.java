package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.dto.ReviewDto;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.entity.Review;
import com.kinjo.Beauthrist_Backend.entity.User;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.ProductRepo;
import com.kinjo.Beauthrist_Backend.repository.ReviewRepo;
import com.kinjo.Beauthrist_Backend.repository.UserRepo;
import com.kinjo.Beauthrist_Backend.service.interf.ReviewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepo reviewRepository;

    @Autowired
    private EntityDtoMapper entityDtoMapper;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private UserRepo userRepository;


    @Override
    public ReviewDto addReview(ReviewDto reviewDto) {
        // Fetch the product and user entities
        Product product = productRepository.findById(reviewDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(reviewDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Map DTO to entity
        Review review = entityDtoMapper.mapReviewToEntity(reviewDto);
        review.setProduct(product);
        review.setUser(user);
        review.setTimestamp(LocalDateTime.now());

        // Save the review
        Review savedReview = reviewRepository.save(review);

        // Map the saved entity back to DTO
        return entityDtoMapper.mapReviewToDto(savedReview);
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll(); // Fetch all reviews
        return reviews.stream()
                .map(entityDtoMapper::mapReviewToDto) // Map each review to DTO
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(entityDtoMapper::mapReviewToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(entityDtoMapper::mapReviewToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByProductIdAndUserId(Long productId, Long userId) {
        List<Review> reviews = reviewRepository.findByProductIdAndUserId(productId, userId);
        return reviews.stream()
                .map(entityDtoMapper::mapReviewToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}

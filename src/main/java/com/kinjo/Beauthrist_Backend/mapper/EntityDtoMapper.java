package com.kinjo.Beauthrist_Backend.mapper;

import com.kinjo.Beauthrist_Backend.dto.*;
import com.kinjo.Beauthrist_Backend.entity.*;
import com.kinjo.Beauthrist_Backend.enums.PaymentStatus;
import com.kinjo.Beauthrist_Backend.repository.OrderRepo;
import com.kinjo.Beauthrist_Backend.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OrderRepo orderRepo;

    public ProductDto mapProductToDtoBasic(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOldPrice(product.getOldPrice());
        dto.setNewPrice(product.getNewPrice());
        dto.setThumbnailImageUrl(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());
        dto.setImageUrls(product.getImages().stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));
        dto.setLikes(product.getLikes());
        dto.setStock(product.getStock());
//        if (product.getUser() != null) {
//            dto.setUserId(product.getUser().getId());
//            dto.setCompanyName(product.getUser().getName());
//        } else {
//            dto.setCompanyName("No Company");
//        }
        if (product.getUser() != null) {
            dto.setUserId(product.getUser().getId());
            dto.setCompanyName(
                    product.getUser().getCompanyName() != null ?
                            product.getUser().getCompanyName() :
                            "No Company"
            );
        } else {
            dto.setCompanyName("No Company");
        }

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategory(product.getCategory().getName()); // Add this line
        } else {
            dto.setCategory("uncategorized"); // Default value
        }
        if (product.getSubCategory() != null) {
            dto.setSubCategoryId(product.getSubCategory().getId());
            dto.setSubCategory(product.getSubCategory().getName()); // Add this line
        } else {
            dto.setSubCategory("uncategorized"); // Default value
        }
        dto.setSizes(product.getSizes().stream().map(ProductSize::getSize).collect(Collectors.toList()));

        // Map colors (with color name and color code)
        if (product.getColors() != null) {
            dto.setColors(product.getColors().stream()
                    .map(color -> new ColorDto(
                            color.getColor(),
                            color.getColorCode() != null ?
                                    color.getColorCode() :
                                    getDefaultColorCode(color.getColor()) // Use shared logic
                    ))
                    .collect(Collectors.toList()));
        }
        dto.setViewCount(product.getViewCount());
        dto.setLastViewedDate(product.getLastViewedDate());


        return dto;
    }

    private String getDefaultColorCode(String colorName) {
        Map<String, String> defaultColors = new HashMap<>();
        // Use lowercase keys for all color names
        defaultColors.put("red", "#FF0000");
        defaultColors.put("blue", "#0000FF");
        defaultColors.put("green", "#00FF00");
        defaultColors.put("black", "#000000");
        defaultColors.put("cyan", "#00FFFF");
        defaultColors.put("magenta", "#FF00FF");
        defaultColors.put("dark gray", "#404040");
        defaultColors.put("gray", "#808080");
        defaultColors.put("light gray", "#C0C0C0");
        defaultColors.put("purple", "#800080");
        defaultColors.put("pink", "#FFC0CB");
        defaultColors.put("brown", "#A52A2A");
        defaultColors.put("gold", "#FFD700");
        defaultColors.put("silver", "#C0C0C0");
        defaultColors.put("orange", "#FFA500");
        defaultColors.put("maroon", "#800000");
        defaultColors.put("tomato", "#FF6347");
        defaultColors.put("orange red", "#FF4500");
        defaultColors.put("chocolate", "#D2691E");
        defaultColors.put("lime green", "#32CD32");
        defaultColors.put("dark green", "#008000");
        defaultColors.put("navy blue", "#000080");
        defaultColors.put("olive", "#808000");
        defaultColors.put("green yellow", "#ADFF2F");
        defaultColors.put("deep sky blue", "#00BFFF");
        defaultColors.put("pale green", "#98FB98");
        defaultColors.put("spring green", "#00FF7F");
        defaultColors.put("light green", "#90EE90");
        defaultColors.put("light blue", "#ADD8E6");
        defaultColors.put("sky blue", "#87CEEB");
        defaultColors.put("power blue", "#B0E0E6");
        defaultColors.put("light cyan", "#E0FFFF");
        defaultColors.put("baby blue", "#89CFF0");
        defaultColors.put("ashes", "#C6C3B5");
        // Add more defaults as needed

        return defaultColors.getOrDefault(colorName.toLowerCase(), "#CCCCCC");
    }




    //    for deals
    public DealDto mapDealToDto(Deal deal) {
        return DealDto.builder()
                .id(deal.getId())
                .product(mapProductToDtoBasic(deal.getProduct()))
                .startDate(deal.getStartDate())
                .endDate(deal.getEndDate())
                .discountPercentage(deal.getDiscountPercentage())
                .active(deal.isActive())
                .build();
    }



public UserDto mapUserToDtoBasic(User user){
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setPhoneNumber(user.getPhoneNumber());
    userDto.setEmail(user.getEmail());
    if (user.getCompanyName() != null) {
        userDto.setCompanyName(user.getCompanyName());
    }
    userDto.setRegDate(user.getCreatedAt());
    userDto.setRole(user.getUserRole().name());
    userDto.setName(user.getName());
    return userDto;

}

    //Address to DTO Basic
    public AddressDto mapAddressToDtoBasic(Address address){
        AddressDto addressDto = new AddressDto();
        addressDto.setId(address.getId());
        addressDto.setCity(address.getCity());
        addressDto.setStreet(address.getStreet());
        addressDto.setState(address.getState());
        addressDto.setCountry(address.getCountry());
        addressDto.setZipcode(address.getZipcode());
        return addressDto;
    }

    //Category to DTO basic

    // Add this method to map SubCategory to SubCategoryDto
    public SubCategoryDto mapSubCategoryToDto(SubCategory subCategory) {
        SubCategoryDto subCategoryDto = new SubCategoryDto();
        subCategoryDto.setId(subCategory.getId());
        subCategoryDto.setName(subCategory.getName());
        subCategoryDto.setCategoryId(subCategory.getCategory().getId());
        return subCategoryDto;
    }

    // Update the mapCategoryToDtoBasic method to include subcategories
    public CategoryDto mapCategoryToDtoBasic(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        if (category.getSubCategories() != null) {
            List<SubCategoryDto> subCategoryDtos = category.getSubCategories().stream()
                    .map(this::mapSubCategoryToDto)
                    .collect(Collectors.toList());
            categoryDto.setSubCategories(subCategoryDtos);
        }
        return categoryDto;
    }


    public UserDto mapUserToDtoPlusAddress(User user){
        UserDto userDto = mapUserToDtoBasic(user);
        if (user.getAddress() != null){
            AddressDto addressDto = mapAddressToDtoBasic(user.getAddress());
            userDto.setAddress(addressDto);
        }
        return userDto;
    }


    //orderItem to DTO plus product
    public OrderItemDto mapOrderItemToDtoPlusProduct(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        // Map basic fields
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setStatus(orderItem.getOrderStatus().name());
        dto.setCreatedAt(orderItem.getCreatedAt());

        // Map selected variants
        dto.setSelectedSize(orderItem.getSize());
        dto.setSelectedColor(orderItem.getColor());

        // Map essential product info
        if (orderItem.getProduct() != null) {
            Product product = orderItem.getProduct();
            dto.setProductName(product.getName());
            dto.setPrice(product.getNewPrice());

            // Get first image URL
            if (!product.getImages().isEmpty()) {
                dto.setProductImageUrl(product.getImages().get(0).getImageUrl());
            }
        }

        return dto;
    }
    //OrderItem to DTO plus product and user
    public OrderItemDto mapOrderItemToDtoPlusProductAndUser(OrderItem orderItem){
        OrderItemDto orderItemDto = mapOrderItemToDtoPlusProduct(orderItem);

        if (orderItem.getUser() != null){
            UserDto userDto = mapUserToDtoPlusAddress(orderItem.getUser());
            orderItemDto.setUser(userDto);
        }
        return orderItemDto;
    }


    //USer to DTO with Address and Order Items History
    public UserDto mapUserToDtoPlusAddressAndOrderHistory(User user) {
        UserDto userDto = mapUserToDtoPlusAddress(user);

        if (user.getOrderItemsList() != null && !user.getOrderItemsList().isEmpty()) {
            userDto.setOrderItemList(user.getOrderItemsList()
                    .stream()
                    .map(this::mapOrderItemToDtoPlusProduct)
                    .collect(Collectors.toList()));
        }
        return userDto;

    }


    public static CustomerSupportDto toDto(CustomerSupport customerSupport) {
        CustomerSupportDto dto = new CustomerSupportDto();
        dto.setId(customerSupport.getId());
        dto.setCustomerId(customerSupport.getCustomerId()); // Add this line
        dto.setCustomerName(customerSupport.getCustomerName()); // Add this line
        dto.setEmail(customerSupport.getEmail());
        dto.setSubject(customerSupport.getSubject());
        dto.setMessage(customerSupport.getMessage());
        dto.setCreatedAt(customerSupport.getCreatedAt());
        dto.setResolved(customerSupport.isResolved());
        dto.setStatus(customerSupport.getStatus()); // Add this line
        return dto;
    }

    public static CustomerSupport toEntity(CustomerSupportDto dto) {
        CustomerSupport customerSupport = new CustomerSupport();
        customerSupport.setCustomerId(dto.getCustomerId()); // Add this line
        customerSupport.setCustomerName(dto.getCustomerName()); // Add this line
        customerSupport.setEmail(dto.getEmail());
        customerSupport.setSubject(dto.getSubject());
        customerSupport.setMessage(dto.getMessage());
        customerSupport.setCreatedAt(dto.getCreatedAt());
        customerSupport.setResolved(dto.isResolved());
        customerSupport.setStatus(dto.getStatus()); // Add this line
        return customerSupport;
    }


    public ReviewDto mapReviewToDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setId(review.getId());
        reviewDto.setRating(review.getRating());
        reviewDto.setComment(review.getComment());
        reviewDto.setTimestamp(review.getTimestamp());

        // Map product ID
        if (review.getProduct() != null) {
            reviewDto.setProductId(review.getProduct().getId());
        }

        // Map user ID and username
        if (review.getUser() != null) {
            reviewDto.setUserId(review.getUser().getId());
            reviewDto.setUserName(review.getUser().getName());
        }

        return reviewDto;
    }

    public Review mapReviewToEntity(ReviewDto reviewDto) {
        Review review = new Review();
        review.setId(reviewDto.getId());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setTimestamp(reviewDto.getTimestamp());

        // Map product ID to Product entity
        if (reviewDto.getProductId() != null) {
            Product product = new Product();
            product.setId(reviewDto.getProductId());
            review.setProduct(product);
        }

        // Map user ID to User entity
        if (reviewDto.getUserId() != null) {
            User user = new User();
            user.setId(reviewDto.getUserId());
            review.setUser(user);
        }

        return review;
    }


    public Payment toEntity(PaymentRequest request) {
        Payment payment = new Payment();

        // Set basic payment details
        payment.setAmount(request.getAmount());
        payment.setEmail(request.getEmail());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(request.getTransactionId());
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        payment.setUser(user);
        if (request.getOrderId() != null) {
            Order order = orderRepo.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            payment.setOrder(order);
        }

        return payment;
    }

    //  Wish list

        public WishlistDto wishlistDto(Wishlist wishlist) {
            WishlistDto dto = new WishlistDto();
            dto.setId(wishlist.getId());
            dto.setUserId(wishlist.getUser().getId());
            dto.setProductId(wishlist.getProduct().getId());
            dto.setProductName(wishlist.getProduct().getName());
            dto.setPrice(wishlist.getProduct().getNewPrice());
            if (wishlist.getProduct().getImages() != null
                    && !wishlist.getProduct().getImages().isEmpty()) {
                dto.setProductImage(wishlist.getProduct().getImages().get(0).getImageUrl());
            } else {
                dto.setProductImage("default-image-url.jpg"); // Fallback URL
            }
            return dto;
        }

    public NewsletterSubscriberDto subscribeToEntity(NewsletterSubscriber subscriber) {
        return NewsletterSubscriberDto.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .isActive(subscriber.getIsActive())
                .subscribed(subscriber.getSubscribed())
                .createdAt(subscriber.getCreatedAt())
                .build();
    }

    public List<NewsletterSubscriberDto> mapSubscribeToDtoList(List<NewsletterSubscriber> subscribers) {
        return subscribers.stream()
                .map(this::subscribeToEntity)
                .collect(Collectors.toList());
    }

}

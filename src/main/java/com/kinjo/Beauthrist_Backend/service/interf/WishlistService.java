package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.WishlistDto;

import java.util.List;

public interface WishlistService {
    WishlistDto addToWishlist(Long userId, Long productId);
    List<WishlistDto> getWishlistByUserId(Long userId);
    void removeFromWishlist(Long userId, Long productId);
}

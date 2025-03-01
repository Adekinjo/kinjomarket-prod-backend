// DealServiceImpl.java
package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.DealDto;
import com.kinjo.Beauthrist_Backend.dto.ProductDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.Deal;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.DealRepo;
import com.kinjo.Beauthrist_Backend.repository.ProductRepo;
import com.kinjo.Beauthrist_Backend.service.interf.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepo dealRepository;
    private final ProductRepo productRepo;
    private final EntityDtoMapper entityDtoMapper;

    @Override
    public Response createDeal(Long productId, DealDto dealDto) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));

        Deal deal = new Deal();
        deal.setProduct(product);
        deal.setStartDate(dealDto.getStartDate());
        deal.setEndDate(dealDto.getEndDate());
        deal.setDiscountPercentage(dealDto.getDiscountPercentage());
        deal.setActive(true);

        Deal savedDeal = dealRepository.save(deal);
        return Response.builder()
                .status(200)
                .deal(entityDtoMapper.mapDealToDto(savedDeal))
                .message("Deal created successfully")
                .build();
    }

    @Override
    public Response updateDeal(Long dealId, DealDto dealDto) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + dealId));

        if (dealDto.getStartDate() != null) deal.setStartDate(dealDto.getStartDate());
        if (dealDto.getEndDate() != null) deal.setEndDate(dealDto.getEndDate());
        if (dealDto.getDiscountPercentage() != null)
            deal.setDiscountPercentage(dealDto.getDiscountPercentage());

        Deal updatedDeal = dealRepository.save(deal);
        return Response.builder()
                .status(200)
                .deal(entityDtoMapper.mapDealToDto(updatedDeal))
                .message("Deal updated successfully")
                .build();
    }

    @Override
    public Response deleteDeal(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + dealId));

        dealRepository.delete(deal);
        return Response.builder()
                .status(200)
                .message("Deal deleted successfully")
                .build();
    }

    @Override
    public Response getAllActiveDeals() {
        List<Deal> activeDeals = dealRepository.findByActiveTrueAndEndDateAfter(LocalDateTime.now());
        List<DealDto> dealDtos = activeDeals.stream()
                .map(entityDtoMapper::mapDealToDto)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .dealList(dealDtos)
                .build();
    }

    @Override
    public Response getDealByProductId(Long productId) {
        Deal deal = dealRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No active deal found for product ID: " + productId));

        return Response.builder()
                .status(200)
                .deal(entityDtoMapper.mapDealToDto(deal))
                .build();
    }

    @Override
    public Response toggleDealStatus(Long dealId, boolean status) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new NotFoundException("Deal not found with ID: " + dealId));

        deal.setActive(status);
        dealRepository.save(deal);

        return Response.builder()
                .status(200)
                .message("Deal status updated to " + status)
                .build();
    }
}
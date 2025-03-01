// DealController.java
package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.DealDto;
import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.service.interf.DealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> createDeal(@RequestBody DealDto dealDto) {
        try {
            return ResponseEntity.ok(dealService.createDeal(dealDto.getProduct().getId(), dealDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/update/{dealId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateDeal(@PathVariable Long dealId, @RequestBody DealDto dealDto) {
        try {
            return ResponseEntity.ok(dealService.updateDeal(dealId, dealDto));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/delete/{dealId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> deleteDeal(@PathVariable Long dealId) {
        return ResponseEntity.ok(dealService.deleteDeal(dealId));
    }

    @GetMapping("/all/active")
    public ResponseEntity<Response> getActiveDeals() {
        return ResponseEntity.ok(dealService.getAllActiveDeals());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Response> getDealByProduct(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(dealService.getDealByProductId(productId));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    @PatchMapping("/{dealId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> toggleDealStatus(
            @PathVariable Long dealId,
            @RequestParam boolean active) {
        return ResponseEntity.ok(dealService.toggleDealStatus(dealId, active));
    }
}
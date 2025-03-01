// DealService.java
package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.DealDto;
import com.kinjo.Beauthrist_Backend.dto.Response;

public interface DealService {
    Response createDeal(Long productId, DealDto dealDto);
    Response updateDeal(Long dealId, DealDto dealDto);
    Response deleteDeal(Long dealId);
    Response getAllActiveDeals();
    Response getDealByProductId(Long productId);
    Response toggleDealStatus(Long dealId, boolean status);
}
package com.kinjo.Beauthrist_Backend.service.interf;

import com.kinjo.Beauthrist_Backend.dto.CustomerSupportDto;

import java.util.List;

// Service: CustomerSupportService
public interface CustomerSupportService {
    CustomerSupportDto updateComplaintStatus(Long id, String status);
    CustomerSupportDto createInquiry(CustomerSupportDto inquiryDto);
    List<CustomerSupportDto> getAllInquiries();
    CustomerSupportDto getInquiryById(Long id);
    void deleteInquiry(Long id);
}


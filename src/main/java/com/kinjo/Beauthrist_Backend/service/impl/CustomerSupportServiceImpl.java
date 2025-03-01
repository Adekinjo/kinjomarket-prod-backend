package com.kinjo.Beauthrist_Backend.service.impl;

import com.kinjo.Beauthrist_Backend.dto.CustomerSupportDto;
import com.kinjo.Beauthrist_Backend.entity.CustomerSupport;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.mapper.EntityDtoMapper;
import com.kinjo.Beauthrist_Backend.repository.CustomerSupportRepo;
import com.kinjo.Beauthrist_Backend.service.interf.CustomerSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerSupportServiceImpl implements CustomerSupportService {

    @Autowired
    private CustomerSupportRepo customerSupportRepository;

    @Override
    public CustomerSupportDto createInquiry(CustomerSupportDto inquiryDto) {
        CustomerSupport inquiry = EntityDtoMapper.toEntity(inquiryDto);
        inquiry.setCreatedAt(LocalDateTime.now());
        inquiry.setResolved(false);
        inquiry.setStatus("CREATED");
        CustomerSupport savedInquiry = customerSupportRepository.save(inquiry);
        return EntityDtoMapper.toDto(savedInquiry);
    }

    @Override
    public List<CustomerSupportDto> getAllInquiries() {
        return customerSupportRepository.findAll().stream()
                .map(EntityDtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerSupportDto getInquiryById(Long id) {
        CustomerSupport inquiry = customerSupportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + id));
        return EntityDtoMapper.toDto(inquiry);
    }

    @Override
    public CustomerSupportDto updateComplaintStatus(Long id, String status) {
        CustomerSupport inquiry = customerSupportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inquiry not found with id: " + id));
        inquiry.setStatus(status); // Update the status
        CustomerSupport updatedInquiry = customerSupportRepository.save(inquiry);
        return EntityDtoMapper.toDto(updatedInquiry);
    }

    @Override
    public void deleteInquiry(Long id) {
        customerSupportRepository.deleteById(id);
    }
}

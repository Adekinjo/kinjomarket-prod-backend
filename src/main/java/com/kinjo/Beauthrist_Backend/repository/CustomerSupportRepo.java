package com.kinjo.Beauthrist_Backend.repository;

import com.kinjo.Beauthrist_Backend.entity.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

// Repository: CustomerSupportQueryRepository
@Repository
public interface CustomerSupportRepo extends JpaRepository<CustomerSupport, Long> {
    List<CustomerSupport> findByCustomerId(String customerId);
}


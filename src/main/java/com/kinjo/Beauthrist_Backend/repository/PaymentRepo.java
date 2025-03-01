package com.kinjo.Beauthrist_Backend.repository;

import com.kinjo.Beauthrist_Backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);
}


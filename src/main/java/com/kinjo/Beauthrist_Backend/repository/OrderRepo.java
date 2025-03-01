package com.kinjo.Beauthrist_Backend.repository;

import com.kinjo.Beauthrist_Backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderRepo extends JpaRepository<Order, Long> {

}


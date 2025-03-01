package com.kinjo.Beauthrist_Backend.repository;

import com.kinjo.Beauthrist_Backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {
}

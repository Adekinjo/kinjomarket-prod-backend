package com.kinjo.Beauthrist_Backend.repository;

import com.kinjo.Beauthrist_Backend.entity.PasswordResetToken;
import com.kinjo.Beauthrist_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);

    void deleteByExpiryDateBefore(LocalDateTime now);
}
package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Token;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Token> findByToken(String token);

    void deleteByExpiresAtBefore(Instant now);
}

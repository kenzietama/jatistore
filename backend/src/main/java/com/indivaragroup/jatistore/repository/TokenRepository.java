package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByUserId(UUID userId);

    Optional<Token> findByToken(String token);
}

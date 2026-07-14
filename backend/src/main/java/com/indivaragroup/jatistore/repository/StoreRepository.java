package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findBySellerId(UUID sellerId);
}

package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT CASE " +
            "  WHEN (SELECT COUNT(*) FROM mst_admins WHERE user_id = :userId) > 0 THEN 'ADMIN' " +
            "  WHEN (SELECT COUNT(*) FROM mst_sellers WHERE user_id = :userId) > 0 THEN 'SELLER' " +
            "  ELSE 'USER' " +
            "END", nativeQuery = true)
    String findUserRole(@Param("userId") UUID userId);

    @Query(value = "SELECT active FROM mst_users u JOIN mst_sellers s ON u.id = s.user_id WHERE u.email = :email", nativeQuery = true)
    boolean isSellerActive(String email);

}

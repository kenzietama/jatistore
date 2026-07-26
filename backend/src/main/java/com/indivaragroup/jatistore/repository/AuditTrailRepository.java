package com.indivaragroup.jatistore.repository;

import com.indivaragroup.jatistore.data.entity.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, UUID>, JpaSpecificationExecutor<AuditTrail> {
    Optional<AuditTrail> findFirstByEntityIdAndActionOrderByCreatedAtDesc(UUID entityId, String action);
}

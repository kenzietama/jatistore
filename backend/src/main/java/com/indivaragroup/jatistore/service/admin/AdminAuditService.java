package com.indivaragroup.jatistore.service.admin;



import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.module.admin.AuditTrailResponse;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AuditTrailRepository auditTrailRepository;
    private final AuthRepository authRepository;

    public Page<AuditTrailResponse> getAuditTrails(String action, String module, UUID userId, Instant startDate, Instant endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<AuditTrail> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null && !action.isEmpty()) predicates.add(cb.equal(root.get("action"), action));
            if (module != null && !module.isEmpty()) predicates.add(cb.equal(root.get("affectedModule"), module));
            if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
            if (startDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            if (endDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditTrail> audits = auditTrailRepository.findAll(spec, pageable);

        return audits.map(audit -> {
            String username = null;
            if (audit.getUserId() != null) {
                username = authRepository.findById(audit.getUserId()).map(User::getUsername).orElse("Unknown");
            }

            Object payloadObj = audit.getPayload();

            return AuditTrailResponse.builder()
                    .id(audit.getId())
                    .userId(audit.getUserId())
                    .username(username)
                    .userRole(audit.getUserRole())
                    .action(audit.getAction())
                    .affectedModule(audit.getAffectedModule())
                    .entityId(audit.getEntityId())
                    .description(audit.getDescription())
                    .payload(payloadObj)
                    .ipAddress(audit.getIpAddress())
                    .createdAt(audit.getCreatedAt())
                    .build();
        });
    }
}

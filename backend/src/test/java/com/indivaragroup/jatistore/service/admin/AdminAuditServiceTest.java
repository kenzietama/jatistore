package com.indivaragroup.jatistore.service.admin;

import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.module.admin.AuditTrailResponse;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminAuditServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private AdminAuditService adminAuditService;

    @Mock
    private Root<AuditTrail> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Test
    void getAuditTrails_shouldReturnPageAndTestSpecification() {
        AuditTrail audit1 = new AuditTrail();
        audit1.setId(UUID.randomUUID());
        audit1.setUserId(UUID.randomUUID());
        audit1.setAction("LOGIN");

        AuditTrail audit2 = new AuditTrail();
        audit2.setId(UUID.randomUUID());

        Page<AuditTrail> page = new PageImpl<>(List.of(audit1, audit2));
        
        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        
        User user = new User();
        user.setId(audit1.getUserId());
        user.setUsername("testuser");
        when(authRepository.findById(audit1.getUserId())).thenReturn(Optional.of(user));

        Instant now = Instant.now();
        Page<AuditTrailResponse> result = adminAuditService.getAuditTrails("LOGIN", "AUTH", audit1.getUserId(), now, now, 0, 10);
        
        assertEquals(2, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        assertNull(result.getContent().get(1).getUsername());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository).findAll(captor.capture(), any(Pageable.class));

        Specification<AuditTrail> spec = captor.getValue();
        
        lenient().when(root.get(anyString())).thenReturn(mock(jakarta.persistence.criteria.Path.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.greaterThanOrEqualTo(any(), any(Instant.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.lessThanOrEqualTo(any(), any(Instant.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }
    
    @Test
    void getAuditTrails_withNullFilters_shouldStillReturnPage() {
        Page<AuditTrail> page = new PageImpl<>(List.of());
        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<AuditTrailResponse> result = adminAuditService.getAuditTrails(null, null, null, null, null, 0, 10);
        assertEquals(0, result.getTotalElements());
        
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository).findAll(captor.capture(), any(Pageable.class));

        Specification<AuditTrail> spec = captor.getValue();
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        Predicate predicate = spec.toPredicate(root, query, cb);
        assertNotNull(predicate);
    }
}

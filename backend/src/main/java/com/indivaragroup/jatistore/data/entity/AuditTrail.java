package com.indivaragroup.jatistore.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trx_audit_trails")
@Getter
@Setter
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_role")
    private String userRole;

    @Column(nullable = false)
    private String action;

    @Column(name = "affected_module", nullable = false)
    private String affectedModule;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
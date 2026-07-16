package com.indivaragroup.jatistore.dto.response.module.admin;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AuditTrailResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String userRole;
    private String action;
    private String affectedModule;
    private UUID entityId;
    private String description;
    private Object payload;
    private String ipAddress;
    private Instant createdAt;
}

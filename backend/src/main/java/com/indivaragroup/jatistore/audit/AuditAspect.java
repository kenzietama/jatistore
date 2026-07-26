package com.indivaragroup.jatistore.audit;

import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import org.springframework.web.util.WebUtils;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditTrailRepository auditTrailRepository;
    private final AuthRepository authRepository;

    @AfterReturning(pointcut = "@annotation(auditAnnotation)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Audit auditAnnotation, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        AuditTrail auditTrail = new AuditTrail();

        // 1. Annotation Data
        auditTrail.setAction(auditAnnotation.action());
        auditTrail.setAffectedModule(auditAnnotation.affectedModule());
        auditTrail.setDescription(auditAnnotation.description());

        // 2. IP Address (Bypass IPv6 for Localhost)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        auditTrail.setIpAddress(ipAddress);

        // 3. Request Payload
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                auditTrail.setPayload(sanitizePayload(payload));
            }
        }

        // 3.5. Extract Entity ID
        // Try extracting from parameter (e.g. @PathVariable UUID id)
        if (joinPoint.getSignature() instanceof org.aspectj.lang.reflect.MethodSignature signature) {
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    if (parameterNames[i].toLowerCase().endsWith("id") && args[i] instanceof java.util.UUID uuid) {
                        auditTrail.setEntityId(uuid);
                        break;
                    }
                }
            }
        }
        // If not found, try extracting from result
        if (auditTrail.getEntityId() == null && result instanceof com.indivaragroup.jatistore.dto.response.RestApiResponse<?> apiResponse) {
            Object data = apiResponse.getRestApiResponseData();
            if (data != null) {
                try {
                    java.lang.reflect.Method getIdMethod = data.getClass().getMethod("getId");
                    Object idValue = getIdMethod.invoke(data);
                    if (idValue instanceof java.util.UUID uuid) {
                        auditTrail.setEntityId(uuid);
                    }
                } catch (Exception ignored) {
                    if (data instanceof java.util.Map map) {
                        for (Object key : map.keySet()) {
                            if (key.toString().toLowerCase().endsWith("id")) {
                                try {
                                    auditTrail.setEntityId(java.util.UUID.fromString(map.get(key).toString()));
                                } catch (Exception ignored2) {}
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 4. User Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            authRepository.findByEmail(email).ifPresent(user -> {
                auditTrail.setUserId(user.getId());
                String role = userDetails.getAuthorities().iterator().next().getAuthority();
                auditTrail.setUserRole(role.replace("ROLE_", ""));
            });
        } else if ("LOGIN".equals(auditAnnotation.action())) {
            try {
                if (auditTrail.getPayload() != null) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"authLoginRequestEmail\"\\s*:\\s*\"([^\"]+)\"").matcher((String)auditTrail.getPayload());
                    if (m.find()) {
                        String email = m.group(1);
                        authRepository.findByEmail(email).ifPresent(user -> {
                            auditTrail.setUserId(user.getId());
                            auditTrail.setUserRole(authRepository.findUserRole(user.getId()));
                        });
                    }
                }
            } catch (Exception ignored) {}
        }

        // 5. Save
        auditTrailRepository.save(auditTrail);

        if ("ORDER_CREATE".equals(auditAnnotation.action())) {
            AuditTrail paidAudit = new AuditTrail();
            paidAudit.setAction("ORDER_PAID");
            paidAudit.setAffectedModule("ORDERS");
            paidAudit.setDescription("Payment successful for order");
            paidAudit.setIpAddress(auditTrail.getIpAddress());
            paidAudit.setPayload(auditTrail.getPayload());
            paidAudit.setEntityId(auditTrail.getEntityId());
            paidAudit.setUserId(auditTrail.getUserId());
            paidAudit.setUserRole(auditTrail.getUserRole());
            auditTrailRepository.save(paidAudit);
        }
    }

    @AfterThrowing(pointcut = "@annotation(auditAnnotation)", throwing = "exception")
    public void logAuditFailure(JoinPoint joinPoint, Audit auditAnnotation, Throwable exception) {
        if ("LOGIN".equals(auditAnnotation.action())) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            AuditTrail auditTrail = new AuditTrail();
            
            auditTrail.setAction("LOGIN_FAILED");
            auditTrail.setAffectedModule("AUTH");
            
            String errorMsg = exception.getMessage();
            if (exception instanceof com.indivaragroup.jatistore.exception.CoreThrowHandler coreThrow) {
                errorMsg = coreThrow.getRestApiError() != null ? coreThrow.getRestApiError().getMessage() : coreThrow.getCustomMessage();
            }
            auditTrail.setDescription("Failed login attempt: " + errorMsg);

            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            auditTrail.setIpAddress(ipAddress);

            ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                    auditTrail.setPayload(sanitizePayload(payload));
                    
                    try {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"authLoginRequestEmail\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
                        if (m.find()) {
                            String email = m.group(1);
                            authRepository.findByEmail(email).ifPresent(user -> {
                                auditTrail.setUserId(user.getId());
                                auditTrail.setUserRole(authRepository.findUserRole(user.getId()));
                            });
                        }
                    } catch (Exception ignored) {}
                }
            }

            auditTrailRepository.save(auditTrail);
        } else if ("ORDER_CREATE".equals(auditAnnotation.action())) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();
            AuditTrail auditTrail = new AuditTrail();
            
            auditTrail.setAction("ORDER_CANCELLED");
            auditTrail.setAffectedModule("ORDERS");
            
            String errorMsg = exception.getMessage();
            if (exception instanceof com.indivaragroup.jatistore.exception.CoreThrowHandler coreThrow) {
                errorMsg = coreThrow.getRestApiError() != null ? coreThrow.getRestApiError().getMessage() : coreThrow.getCustomMessage();
            }
            auditTrail.setDescription("Order cancelled due to payment failure: " + errorMsg);

            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            auditTrail.setIpAddress(ipAddress);

            ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
            if (wrapper != null) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                    auditTrail.setPayload(sanitizePayload(payload));
                }
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                authRepository.findByEmail(email).ifPresent(user -> {
                    auditTrail.setUserId(user.getId());
                    String role = userDetails.getAuthorities().iterator().next().getAuthority();
                    auditTrail.setUserRole(role.replace("ROLE_", ""));
                });
            }

            auditTrailRepository.save(auditTrail);
        }
    }

    private String sanitizePayload(String payload) {
        // Mask any key containing 'password' (case insensitive)
        return payload.replaceAll("(?i)\"([^\"]*password[^\"]*)\"\\s*:\\s*\"[^\"]+\"", "\"$1\":\"*****\"");
    }
}

package com.indivaragroup.jatistore.audit;

import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

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

        // 1. Data dari Anotasi
        auditTrail.setAction(auditAnnotation.action());
        auditTrail.setAffectedModule(auditAnnotation.affectedModule());
        auditTrail.setDescription(auditAnnotation.description());

        // 2. IP Address
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        auditTrail.setIpAddress(ipAddress);

        // 3. Request Payload
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                auditTrail.setPayload(sanitizePayload(payload));
            }
        }

        // 4. User Context dari Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            // Ambil ID asli dari database berdasarkan email JWT
            authRepository.findByEmail(email).ifPresent(user -> {
                auditTrail.setUserId(user.getId());
                // Ambil Role pertama (misal: ROLE_SELLER -> hilangkan "ROLE_")
                String role = userDetails.getAuthorities().iterator().next().getAuthority();
                auditTrail.setUserRole(role.replace("ROLE_", ""));
            });
        }

        // 5. Simpan
        auditTrailRepository.save(auditTrail);
    }

    private String sanitizePayload(String payload) {
        // Sensor password jika ada di dalam request
        return payload.replaceAll("\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"*****\"");
    }
}

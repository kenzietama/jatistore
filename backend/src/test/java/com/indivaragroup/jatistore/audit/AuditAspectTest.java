package com.indivaragroup.jatistore.audit;

import com.indivaragroup.jatistore.data.entity.AuditTrail;
import com.indivaragroup.jatistore.data.entity.User;
import com.indivaragroup.jatistore.dto.response.RestApiResponse;
import com.indivaragroup.jatistore.exception.CoreThrowHandler;
import com.indivaragroup.jatistore.repository.AuditTrailRepository;
import com.indivaragroup.jatistore.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditAspectTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private AuditAspect auditAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Audit auditAnnotation;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes attributes;

    @Mock
    private ContentCachingRequestWrapper wrapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private MockedStatic<RequestContextHolder> requestContextHolderMock;
    private MockedStatic<SecurityContextHolder> securityContextHolderMock;
    private MockedStatic<WebUtils> webUtilsMock;

    @BeforeEach
    void setUp() {
        requestContextHolderMock = Mockito.mockStatic(RequestContextHolder.class);
        securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
        webUtilsMock = Mockito.mockStatic(WebUtils.class);

        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
        lenient().when(attributes.getRequest()).thenReturn(request);

        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        
        lenient().when(auditAnnotation.action()).thenReturn("TEST_ACTION");
        lenient().when(auditAnnotation.affectedModule()).thenReturn("TEST_MODULE");
        lenient().when(auditAnnotation.description()).thenReturn("TEST_DESC");
    }

    @AfterEach
    void tearDown() {
        requestContextHolderMock.close();
        securityContextHolderMock.close();
        webUtilsMock.close();
    }

    @Test
    void logAuditActivity_withoutSecurityContext() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        webUtilsMock.when(() -> WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class)).thenReturn(null);

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());

        AuditTrail trail = captor.getValue();
        assertEquals("TEST_ACTION", trail.getAction());
        assertEquals("192.168.1.1", trail.getIpAddress());
        assertNull(trail.getUserId());
    }

    @Test
    void logAuditActivity_withIpv6Localhost() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertEquals("127.0.0.1", captor.getValue().getIpAddress());
    }

    @Test
    void logAuditActivity_withPayloadAndSecurityContext() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        webUtilsMock.when(() -> WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class)).thenReturn(wrapper);
        when(wrapper.getContentAsByteArray()).thenReturn("{\"password\":\"secret\",\"other\":\"data\"}".getBytes());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("admin@test.com");
        
        GrantedAuthority auth = () -> "ROLE_ADMIN";
        doReturn(List.of(auth)).when(userDetails).getAuthorities();

        User user = new User();
        user.setId(UUID.randomUUID());
        when(authRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());

        AuditTrail trail = captor.getValue();
        assertEquals("10.0.0.1", trail.getIpAddress());
        assertEquals(user.getId(), trail.getUserId());
        assertEquals("ADMIN", trail.getUserRole());
        assertTrue(trail.getPayload().toString().contains("\"password\":\"*****\""));
    }

    @Test
    void logAuditActivity_extractIdFromArgs() {
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getParameterNames()).thenReturn(new String[]{"orderId"});
        
        UUID expectedId = UUID.randomUUID();
        when(joinPoint.getArgs()).thenReturn(new Object[]{expectedId});

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertEquals(expectedId, captor.getValue().getEntityId());
    }

    @Test
    void logAuditActivity_extractIdFromResult() {
        UUID expectedId = UUID.randomUUID();
        Map<String, UUID> data = Map.of("flashSaleId", expectedId);
        RestApiResponse<Map<String, UUID>> response = RestApiResponse.<Map<String, UUID>>builder()
                .restApiResponseData(data)
                .build();

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, response);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertEquals(expectedId, captor.getValue().getEntityId());
    }

    @Test
    void logAuditActivity_loginAction() {
        when(auditAnnotation.action()).thenReturn("LOGIN");
        
        webUtilsMock.when(() -> WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class)).thenReturn(wrapper);
        String payload = "{\"authLoginRequestEmail\":\"user@test.com\"}";
        when(wrapper.getContentAsByteArray()).thenReturn(payload.getBytes());

        User user = new User();
        user.setId(UUID.randomUUID());
        when(authRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(authRepository.findUserRole(user.getId())).thenReturn("SELLER");

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertEquals(user.getId(), captor.getValue().getUserId());
        assertEquals("SELLER", captor.getValue().getUserRole());
    }

    @Test
    void logAuditFailure_loginFailed() {
        when(auditAnnotation.action()).thenReturn("LOGIN");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");

        webUtilsMock.when(() -> WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class)).thenReturn(wrapper);
        String payload = "{\"authLoginRequestEmail\":\"fail@test.com\"}";
        when(wrapper.getContentAsByteArray()).thenReturn(payload.getBytes());

        User user = new User();
        user.setId(UUID.randomUUID());
        when(authRepository.findByEmail("fail@test.com")).thenReturn(Optional.of(user));
        when(authRepository.findUserRole(user.getId())).thenReturn("ADMIN");

        CoreThrowHandler ex = mock(CoreThrowHandler.class);
        com.indivaragroup.jatistore.dto.utility.RestApiError error = com.indivaragroup.jatistore.dto.utility.RestApiError.AUT_0004;
        when(ex.getRestApiError()).thenReturn(error);

        auditAspect.logAuditFailure(joinPoint, auditAnnotation, ex);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        AuditTrail trail = captor.getValue();
        
        assertEquals("LOGIN_FAILED", trail.getAction());
        assertEquals("AUTH", trail.getAffectedModule());
        assertEquals(user.getId(), trail.getUserId());
        assertEquals("ADMIN", trail.getUserRole());
        assertTrue(trail.getDescription().contains(error.getMessage()));
    }
    
    @Test
    void logAuditActivity_nullAttributes() {
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);
        verify(auditTrailRepository, never()).save(any());
    }
    
    @Test
    void logAuditFailure_nullAttributes() {
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
        auditAspect.logAuditFailure(joinPoint, auditAnnotation, new Exception());
        verify(auditTrailRepository, never()).save(any());
    }

    @Test
    void logAuditActivity_emptyBuffer() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        webUtilsMock.when(() -> WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class)).thenReturn(wrapper);
        when(wrapper.getContentAsByteArray()).thenReturn(new byte[0]);

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);
        
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertNull(captor.getValue().getPayload());
    }
    
    @Test
    void logAuditActivity_nullParameterNames() {
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getParameterNames()).thenReturn(null);

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, null);
        
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertNull(captor.getValue().getEntityId());
    }
    
    @Test
    void logAuditActivity_extractIdFromObjectWithoutGetIdMethod() {
        Object responseData = new Object();
        RestApiResponse<Object> response = RestApiResponse.<Object>builder()
                .restApiResponseData(responseData)
                .build();

        auditAspect.logAuditActivity(joinPoint, auditAnnotation, response);
        
        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository).save(captor.capture());
        assertNull(captor.getValue().getEntityId());
    }
}

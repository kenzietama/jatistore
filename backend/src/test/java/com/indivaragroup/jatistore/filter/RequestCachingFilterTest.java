package com.indivaragroup.jatistore.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestCachingFilterTest {

    private RequestCachingFilter filter;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ServletResponse servletResponse;

    @BeforeEach
    void setUp() {
        filter = new RequestCachingFilter();
    }

    @Test
    void doFilter_withGetRequest_shouldNotWrap() throws ServletException, IOException {
        when(httpServletRequest.getMethod()).thenReturn("GET");

        filter.doFilter(httpServletRequest, servletResponse, filterChain);

        verify(filterChain).doFilter(httpServletRequest, servletResponse);
    }

    @Test
    void doFilter_withPostRequest_shouldWrap() throws ServletException, IOException {
        when(httpServletRequest.getMethod()).thenReturn("POST");

        filter.doFilter(httpServletRequest, servletResponse, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(servletResponse));

        assertInstanceOf(ContentCachingRequestWrapper.class, captor.getValue());
    }

    @Test
    void doFilter_withNonHttpServletRequest_shouldNotWrap() throws ServletException, IOException {
        ServletRequest nonHttpRequest = mock(ServletRequest.class);

        filter.doFilter(nonHttpRequest, servletResponse, filterChain);

        verify(filterChain).doFilter(nonHttpRequest, servletResponse);
    }
}

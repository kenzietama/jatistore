package com.indivaragroup.jatistore.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            String method = httpServletRequest.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                chain.doFilter(request, response);
            } else {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpServletRequest, 100000);
                chain.doFilter(wrappedRequest, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}

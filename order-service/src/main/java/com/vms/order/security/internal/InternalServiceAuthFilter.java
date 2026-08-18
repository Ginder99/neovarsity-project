package com.vms.order.security.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_CALLERS = Set.of("payment-service");

    private final HmacVerifier hmacVerifier;

    public InternalServiceAuthFilter(HmacVerifier hmacVerifier) {
        this.hmacVerifier = hmacVerifier;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/orders/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        String serviceName = cachedRequest.getHeader("X-Service-Name");
        String timestamp = cachedRequest.getHeader("X-Internal-Timestamp");
        String signature = cachedRequest.getHeader("X-Internal-Signature");

        if (serviceName == null || !ALLOWED_CALLERS.contains(serviceName)) {
            log.warn("Rejected internal call: unknown or missing service name '{}' for {}",
                    serviceName, cachedRequest.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unknown caller");
            return;
        }

        String body = cachedRequest.getCachedBodyAsString();
        String path = cachedRequest.getRequestURI();

        boolean valid = hmacVerifier.isValid(cachedRequest.getMethod(), path, timestamp, body, signature);

        if (!valid) {
            log.warn("Rejected internal call: invalid or expired signature for {} from '{}'",
                    path, serviceName);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }
}

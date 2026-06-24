package com.example.auth.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

// filter that restricts requests based on client ip address
@Component
@Order(1)
public class IpFilter extends HttpFilter {

    @Value("${security.ip-filter.enabled:true}")
    private boolean enabled;

    @Value("${security.ip-filter.allowed-ips:127.0.0.1,::1,0:0:0:0:0:0:0:1}")
    private List<String> allowedIps;

    @Override
    protected void doFilter(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        // If IP filtering is disabled in config, bypass check
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        // get the remote ip address of the incoming request
        String ip = request.getRemoteAddr();

        // block the request with 403 if ip is not in the allowed list
        if (!allowedIps.contains(ip)) {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied from IP: " + ip + "\"}");
            return;
        }

        // ip is allowed, pass request to next filter in chain
        chain.doFilter(request, response);
    }
}
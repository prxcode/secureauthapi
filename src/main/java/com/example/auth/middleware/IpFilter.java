package com.example.auth.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

// filter that restricts requests based on client ip address
@Component
public class IpFilter extends HttpFilter {

    // allowed ips: ipv4 loopback (127.0.0.1) and ipv6 loopback (::1) for localhost
    // access
    private List<String> allowedIps = List.of("127.0.0.1", "::1", "0:0:0:0:0:0:0:1");

    protected void doFilter(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        // get the remote ip address of the incoming request
        String ip = request.getRemoteAddr();

        // block the request with 403 if ip is not in the allowed list
        if (!allowedIps.contains(ip)) {
            response.setStatus(403);
            return;
        }

        // ip is allowed, pass request to next filter in chain
        chain.doFilter(request, response);
    }
}
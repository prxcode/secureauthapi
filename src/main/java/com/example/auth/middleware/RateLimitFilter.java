package com.example.auth.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    // stores request counts per ip
    private final ConcurrentHashMap<String,Integer> requestCounts =
            new ConcurrentHashMap<>();

    private static final int LIMIT = 10;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        String ip = request.getRemoteAddr();

        requestCounts.putIfAbsent(ip,0);

        int count = requestCounts.get(ip);

        // block request if limit exceeded
        if(count > LIMIT){
            ((HttpServletResponse)response)
                    .sendError(429,"too many requests");
            return;
        }

        requestCounts.put(ip,count+1);

        chain.doFilter(request,response);
    }
}
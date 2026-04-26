package edu.tcu.cs.projectpulse;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SpaFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();

        // Forward SPA routes to index.html so Vue Router handles them.
        // Static files (contain a dot) and API/actuator paths pass through normally.
        if (!uri.startsWith("/api/") && !uri.startsWith("/actuator/") && !uri.contains(".")) {
            request.getRequestDispatcher("/index.html").forward(req, resp);
            return;
        }
        chain.doFilter(req, resp);
    }
}

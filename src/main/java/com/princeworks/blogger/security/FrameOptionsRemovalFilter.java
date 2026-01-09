package com.princeworks.blogger.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class FrameOptionsRemovalFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // For PDFs and author images, wrap response to intercept X-Frame-Options header
        if (requestPath != null && (
            requestPath.startsWith("/pdfs/") ||
            requestPath.startsWith("/author-images/")
        )) {
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
                @Override
                public void setHeader(String name, String value) {
                    // Block X-Frame-Options header for PDFs and images
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setHeader(name, value);
                    }
                }
                
                @Override
                public void addHeader(String name, String value) {
                    // Block X-Frame-Options header for PDFs and images
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.addHeader(name, value);
                    }
                }
                
                @Override
                public void setIntHeader(String name, int value) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setIntHeader(name, value);
                    }
                }
                
                @Override
                public void addIntHeader(String name, int value) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.addIntHeader(name, value);
                    }
                }
            };
            
            filterChain.doFilter(request, wrappedResponse);
            
            // Set Content-Security-Policy to allow framing from any origin
            if (!response.isCommitted()) {
                response.setHeader("Content-Security-Policy", "frame-ancestors *");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

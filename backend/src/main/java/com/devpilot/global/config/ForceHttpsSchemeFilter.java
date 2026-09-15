package com.devpilot.global.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ForceHttpsSchemeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletRequest wrapped = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getScheme() {
                return "https";
            }
            @Override
            public boolean isSecure() {
                return true;
            }
        };
        chain.doFilter(wrapped, response);
    }
}
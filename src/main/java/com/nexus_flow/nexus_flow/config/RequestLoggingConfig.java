package com.nexus_flow.nexus_flow.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Configuration
public class RequestLoggingConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    public static class RequestLoggingFilter implements Filter {

        private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            logger.info("Incoming request: {} {} from Origin: {}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpRequest.getHeader("Origin"));

            chain.doFilter(request, response);

            logger.info("Response status: {} for {} {}",
                    httpResponse.getStatus(),
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI());
        }
    }
}
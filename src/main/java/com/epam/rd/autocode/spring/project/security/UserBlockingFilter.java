package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBlockingFilter extends OncePerRequestFilter {
    private final BlockedClientRepository blockedClientRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();

            if (blockedClientRepository.existsByEmail(email)) {
                log.warn("Security Filter: Detected active session for BLOCKED user '{}'. Invalidating session and redirecting.", email);

                SecurityContextHolder.clearContext();

                request.getSession().invalidate();

                response.sendRedirect("/login?blocked");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
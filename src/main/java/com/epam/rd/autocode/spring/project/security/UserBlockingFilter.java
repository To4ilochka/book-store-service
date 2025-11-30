package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
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
    private static final String ROLE_CLIENT = "ROLE_" + Role.CLIENT.name();
    private final ClientService clientService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && isClient(auth)) {
            String email = auth.getName();

            try {
                if (clientService.getClientByEmail(email).isBlocked()) {
                    log.warn("Security Filter: Detected active session for BLOCKED user '{}'. Invalidating session.", email);
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();
                    response.sendRedirect("/login?blocked");
                    return;
                }
            } catch (Exception e) {
                log.warn("Could not check blocking status for user {}: {}", email, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isClient(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_CLIENT));
    }
}
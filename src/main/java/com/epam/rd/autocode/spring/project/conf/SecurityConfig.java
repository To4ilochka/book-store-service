package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.security.UserBlockingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final UserBlockingFilter userBlockingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain...");

        http
                // .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole(Role.EMPLOYEE.name())
                        .requestMatchers("/clients/**").hasRole(Role.EMPLOYEE.name())
                        .requestMatchers("/orders", "/orders/*/confirm").hasRole(Role.EMPLOYEE.name())
                        .requestMatchers("/cart/**").hasRole(Role.CLIENT.name())
                        .requestMatchers("/orders/create", "/orders/my").hasRole(Role.CLIENT.name())
                        .requestMatchers("/client/**").hasRole(Role.CLIENT.name())
                        .anyRequest().authenticated()
                )
                .addFilterAfter(userBlockingFilter, SecurityContextHolderFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            String username = request.getParameter("username");
                            String errorParam = "error";

                            if (exception instanceof LockedException) {
                                log.warn("Login blocked for user '{}': Account is locked", username);
                                errorParam = "blocked";
                            } else {
                                log.info("Failed login attempt for user '{}': {}", username, exception.getMessage());
                            }

                            response.sendRedirect("/login?" + errorParam);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
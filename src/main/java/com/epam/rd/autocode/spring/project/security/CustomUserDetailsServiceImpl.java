package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        Optional<Client> clientOpt = clientRepository.findByEmail(username);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            log.info("User found: {} (Blocked: {})", client.getEmail(), client.isBlocked());

            return createSpringUser(
                    client.getEmail(),
                    client.getPassword(),
                    Role.CLIENT,
                    client.getName(),
                    client.isBlocked()
            );
        }

        Optional<Employee> employee = employeeRepository.findByEmail(username);
        if (employee.isPresent()) {
            log.info("User found in Employee DB: {}", username);
            return createSpringUser(
                    employee.get().getEmail(),
                    employee.get().getPassword(),
                    Role.EMPLOYEE,
                    employee.get().getName(),
                    false
            );
        }

        log.warn("User login failed: '{}' not found in any database", username);
        throw new UsernameNotFoundException("User not found with email: " + username);
    }

    private SecurityUser createSpringUser(String email, String password, Role role, String name, boolean isBlocked) {
        return new SecurityUser(
                email,
                password,
                true,
                true,
                true,
                !isBlocked,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())),
                name
        );
    }
}
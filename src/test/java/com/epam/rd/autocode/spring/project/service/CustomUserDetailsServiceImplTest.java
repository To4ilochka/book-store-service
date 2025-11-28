package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.BlockedClientRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BlockedClientRepository blockedClientRepository;

    @InjectMocks
    private CustomUserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnClient_WhenClientExistsAndNotBlocked() {
        String email = "client@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("pass");
        client.setName("Client Name");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByEmail(email)).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
    }

    @Test
    void loadUserByUsername_ShouldReturnLockedClient_WhenClientIsBlocked() {
        String email = "blocked@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setPassword("pass");
        client.setName("Blocked Name");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(blockedClientRepository.existsByEmail(email)).thenReturn(true);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_ShouldReturnEmployee_WhenClientNotFoundButEmployeeExists() {
        String email = "emp@test.com";
        Employee employee = new Employee();
        employee.setEmail(email);
        employee.setPassword("empPass");
        employee.setName("Emp Name");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String email = "ghost@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }
}
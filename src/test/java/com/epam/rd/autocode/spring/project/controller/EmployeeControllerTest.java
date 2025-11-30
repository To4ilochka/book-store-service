package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.security.SecurityUser;
import com.epam.rd.autocode.spring.project.security.UserBlockingFilter;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserBlockingFilter userBlockingFilter;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userBlockingFilter).doFilter(any(), any(), any());
    }

    private Authentication getAuthentication(String role, String username) {
        SecurityUser user = new SecurityUser(
                username,
                "password",
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                "Test Employee Name"
        );
        return new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
    }

    @Test
    void getAllEmployees_ShouldReturnListView() throws Exception {
        when(employeeService.getAllEmployees(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/employees")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/list"))
                .andExpect(model().attributeExists("employees"));
    }

    @Test
    void addEmployeeForm_ShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/employees/add")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/add"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    void addEmployee_Success_ShouldRedirectToEmployees() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com")))
                        .param("name", "New Emp")
                        .param("email", "new@test.com")
                        .param("password", "Pass1234")
                        .param("phone", "1234567890")
                        .param("birthDate", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).addEmployee(any(EmployeeDTO.class));
    }

    @Test
    void addEmployee_ValidationErrors_ShouldReturnAddView() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com")))
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/add"));
    }

    @Test
    void deleteEmployee_Success_ShouldRedirectToEmployees() throws Exception {
        mockMvc.perform(post("/employees/delete")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "admin@test.com")))
                        .param("email", "other@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).deleteEmployeeByEmail("other@test.com");
    }

    @Test
    void deleteEmployee_SelfDelete_ShouldRedirectWithError() throws Exception {
        mockMvc.perform(post("/employees/delete")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "admin@test.com")))
                        .param("email", "admin@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees?error=self_delete"));

        verify(employeeService, never()).deleteEmployeeByEmail(anyString());
    }
}
package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.security.UserBlockingFilter;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void home_ShouldReturnIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getLoginForm_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void getRegisterForm_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void register_Success_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "new@test.com")
                        .param("password", "Pass1234")
                        .param("name", "New User")
                        .param("balance", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(clientService).addClient(any(ClientDTO.class));
    }

    @Test
    void register_ValidationErrors_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_UserExists_ShouldReturnRegisterView_ViaGlobalHandler() throws Exception {
        doThrow(new AlreadyExistException("User exists"))
                .when(clientService).addClient(any(ClientDTO.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "exist@test.com")
                        .param("password", "Pass1234")
                        .param("name", "User")
                        .param("balance", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
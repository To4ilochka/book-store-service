package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.security.SecurityUser;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
class ClientControllerTest {

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

    private Authentication getAuthentication(String role, String username) {
        SecurityUser user = new SecurityUser(
                username,
                "password",
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                "Test User Name"
        );
        return new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
    }

    @Test
    void getProfile_ShouldReturnProfileView() throws Exception {
        when(clientService.getClientByEmail("client@test.com")).thenReturn(new ClientDTO());

        mockMvc.perform(get("/client/profile")
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("client/profile"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    void editProfileForm_ShouldReturnEditView() throws Exception {
        when(clientService.getClientByEmail("client@test.com")).thenReturn(new ClientDTO());

        mockMvc.perform(get("/client/profile/edit")
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("client/edit"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    void editProfile_Success_ShouldRedirectToProfile() throws Exception {
        mockMvc.perform(post("/client/profile/edit")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com")))
                        .param("name", "Updated Name")
                        .param("email", "client@test.com")
                        .param("password", "Pass1234")
                        .param("balance", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/profile"));

        verify(clientService).updateClientByEmail(eq("client@test.com"), any(ClientDTO.class));
    }

    @Test
    void editProfile_ValidationErrors_ShouldReturnEditView() throws Exception {
        mockMvc.perform(post("/client/profile/edit")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com")))
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("client/edit"));
    }

    @Test
    void deleteClient_ShouldRedirectToLogout() throws Exception {
        mockMvc.perform(post("/client/delete")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        verify(clientService).deleteClientByEmail("client@test.com");
    }

    @Test
    void getAllClients_ShouldReturnListView() throws Exception {

        when(clientService.getAllClients(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/clients")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "employee@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("client/list_admin"))
                .andExpect(model().attributeExists("clients", "blockedEmails"));
    }

    @Test
    void blockClient_ShouldRedirectToClients() throws Exception {
        mockMvc.perform(post("/clients/block")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "employee@test.com")))
                        .param("email", "bad@user.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).blockClient("bad@user.com");
    }

    @Test
    void unblockClient_ShouldRedirectToClients() throws Exception {
        mockMvc.perform(post("/clients/unblock")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "employee@test.com")))
                        .param("email", "good@user.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).unblockClient("good@user.com");
    }

    @Test
    void topUpBalance_ShouldRedirectToProfile() throws Exception {
        mockMvc.perform(post("/client/topup")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com")))
                        .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/profile"));

        verify(clientService).topUpBalance(eq("client@test.com"), any(BigDecimal.class));
    }
}
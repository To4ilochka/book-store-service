package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.security.SecurityUser;
import com.epam.rd.autocode.spring.project.security.UserBlockingFilter;
import com.epam.rd.autocode.spring.project.service.CartService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

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

    private Authentication getAuthentication(String role) {
        SecurityUser user = new SecurityUser(
                "user",
                "password",
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                "Test User"
        );
        return new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
    }

    @Test
    void showCart_WithUser_ShouldReturnCartViewWithBalance() throws Exception {
        when(cartService.getCartDetails()).thenReturn(Collections.emptyMap());
        when(cartService.getTotalPrice()).thenReturn(BigDecimal.ZERO);

        ClientDTO client = new ClientDTO();
        client.setBalance(new BigDecimal("100.00"));
        when(clientService.getClientByEmail("user")).thenReturn(client);

        mockMvc.perform(get("/cart")
                        .with(authentication(getAuthentication("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/cart"))
                .andExpect(model().attributeExists("cartItems", "totalPrice", "balance"));
    }

    @Test
    void showCart_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void addToCart_ShouldRedirectToBooks_WhenNoReferer() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("bookName", "Java Book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(cartService).addBook("Java Book");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void addToCart_ShouldRedirectToCart_WhenRefererIsCart() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .header("referer", "http://localhost/cart")
                        .param("bookName", "Java Book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).addBook("Java Book");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void decreaseQuantity_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/decrease")
                        .with(csrf())
                        .param("bookName", "Java Book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).decreaseQuantity("Java Book");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void removeFromCart_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/remove")
                        .with(csrf())
                        .param("bookName", "Java Book"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeItem("Java Book");
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clearCart_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/clear")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).clearCart();
    }
}
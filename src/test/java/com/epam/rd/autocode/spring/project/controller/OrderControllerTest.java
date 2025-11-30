package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.conf.SecurityConfig;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.security.SecurityUser;
import com.epam.rd.autocode.spring.project.security.UserBlockingFilter;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

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
    void getAllOrders_ShouldReturnListView() throws Exception {
        when(orderService.getAllOrders(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/orders")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/list"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void createOrder_Success_ShouldRedirectToMyOrders() throws Exception {
        BookDTO book = new BookDTO();
        book.setName("Java");
        when(cartService.getCartDetails()).thenReturn(Map.of(book, 1));
        when(cartService.getTotalPrice()).thenReturn(BigDecimal.TEN);

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/my"));

        verify(orderService).createOrder(eq("client@test.com"), anyMap(), eq(BigDecimal.TEN));
        verify(cartService).clearCart();
    }

    @Test
    void createOrder_EmptyCart_ShouldRedirectToBooks() throws Exception {
        when(cartService.getCartDetails()).thenReturn(Collections.emptyMap());

        mockMvc.perform(post("/orders/create")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void getMyOrders_ShouldReturnMyOrdersView() throws Exception {
        when(orderService.getOrdersByClient("client@test.com")).thenReturn(List.of(new OrderDTO()));

        mockMvc.perform(get("/orders/my")
                        .with(authentication(getAuthentication("ROLE_CLIENT", "client@test.com"))))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/my_orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void confirmOrder_ShouldRedirectToOrders() throws Exception {
        mockMvc.perform(post("/orders/1/confirm")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE", "emp@test.com"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).confirmOrder(1L, "emp@test.com");
    }
}
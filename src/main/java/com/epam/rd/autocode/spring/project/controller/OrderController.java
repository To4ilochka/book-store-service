package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping
    public String getAllOrders(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "employee") String sort,
                               @RequestParam(defaultValue = "asc") String dir,
                               Model model) {

        log.debug("Fetching all orders. Page: {}, Sort: {}", page, sort);

        Page<OrderDTO> orderPage = orderService.getAllOrders(page, size, sort, dir);

        model.addAttribute("orders", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", dir.equals("asc") ? "desc" : "asc");

        return "orders/list";
    }

    @PostMapping("/create")
    public String createOrder(Principal principal, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        Map<BookDTO, Integer> cartDetails = cartService.getCartDetails();

        if (cartDetails.isEmpty()) {
            log.warn("User '{}' attempted to create an order with an empty cart", email);
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
            return "redirect:/books";
        }

        orderService.createOrder(email, cartDetails, cartService.getTotalPrice());

        cartService.clearCart();

        log.info("Order successfully created for user '{}'", email);
        return "redirect:/orders/my";
    }

    @GetMapping("/my")
    public String getMyOrders(Model model, Principal principal) {
        String email = principal.getName();
        log.debug("Fetching orders for client: {}", email);

        List<OrderDTO> myOrders = orderService.getOrdersByClient(email);
        model.addAttribute("orders", myOrders);

        return "orders/my_orders";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder(@PathVariable("id") Long id, Principal principal) {
        log.info("Employee '{}' confirmed order ID: {}", principal.getName(), id);
        orderService.confirmOrder(id, principal.getName());
        return "redirect:/orders";
    }
}
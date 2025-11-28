package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final ClientService clientService;

    @GetMapping
    public String showCart(Model model, Principal principal) {
        model.addAttribute("cartItems", cartService.getCartDetails());
        model.addAttribute("totalPrice", cartService.getTotalPrice());

        if (principal != null) {
            ClientDTO client = clientService.getClientByEmail(principal.getName());
            model.addAttribute("balance", client.getBalance());
        }

        return "orders/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("bookName") String bookName,
                            @RequestHeader(value = "referer", required = false) String referer,
                            RedirectAttributes redirectAttributes) {
        log.debug("Added book to cart: {}", bookName);
        cartService.addBook(bookName);

        redirectAttributes.addFlashAttribute("cartMessage", bookName + " has been added to your cart!");

        if (referer != null && referer.endsWith("/cart")) {
            return "redirect:/cart";
        }
        return "redirect:" + (referer != null ? referer : "/books");
    }

    @PostMapping("/decrease")
    public String decreaseQuantity(@RequestParam("bookName") String bookName) {
        cartService.decreaseQuantity(bookName);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("bookName") String bookName) {
        log.debug("Removed book from cart: {}", bookName);
        cartService.removeItem(bookName);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart() {
        log.debug("Cart cleared");
        cartService.clearCart();
        return "redirect:/cart";
    }
}
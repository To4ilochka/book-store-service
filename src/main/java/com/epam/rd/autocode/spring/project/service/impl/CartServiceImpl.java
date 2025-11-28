package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.session.Cart;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final Cart cart;
    private final BookService bookService;

    @Override
    public void addBook(String bookName) {
        log.debug("Adding book to cart: {}", bookName);
        if (!cart.getBooksCache().containsKey(bookName)) {
            BookDTO book = bookService.getBookByName(bookName);
            cart.getBooksCache().put(bookName, book);
        }

        cart.getItems().put(bookName, cart.getItems().getOrDefault(bookName, 0) + 1);
    }

    @Override
    public void decreaseQuantity(String bookName) {
        log.debug("Decreasing quantity for: {}", bookName);
        if (cart.getItems().containsKey(bookName)) {
            int count = cart.getItems().get(bookName);
            if (count > 1) {
                cart.getItems().put(bookName, count - 1);
            } else {
                removeItem(bookName);
            }
        }
    }

    @Override
    public void removeItem(String bookName) {
        log.debug("Removing item from cart: {}", bookName);
        cart.getItems().remove(bookName);
        cart.getBooksCache().remove(bookName);
    }

    @Override
    public void clearCart() {
        log.debug("Clearing cart");
        cart.getItems().clear();
        cart.getBooksCache().clear();
    }

    @Override
    public Map<BookDTO, Integer> getCartDetails() {
        Map<BookDTO, Integer> details = new LinkedHashMap<>();

        cart.getItems().forEach((name, quantity) -> {
            BookDTO book = cart.getBooksCache().get(name);
            details.put(book, quantity);
        });

        return details;
    }

    @Override
    public BigDecimal getTotalPrice() {
        return cart.getItems().entrySet().stream()
                .map(entry -> {
                    BookDTO book = cart.getBooksCache().get(entry.getKey());
                    return book.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
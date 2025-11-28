package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;

import java.math.BigDecimal;
import java.util.Map;

public interface CartService {
    void addBook(String bookName);

    void decreaseQuantity(String bookName);

    void removeItem(String bookName);

    void clearCart();

    Map<BookDTO, Integer> getCartDetails();

    BigDecimal getTotalPrice();
}

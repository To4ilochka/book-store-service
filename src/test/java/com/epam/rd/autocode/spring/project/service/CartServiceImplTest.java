package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.session.Cart;
import com.epam.rd.autocode.spring.project.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private Cart cart;

    @Mock
    private BookService bookService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Map<String, Integer> itemsMap;
    private Map<String, BookDTO> cacheMap;

    @BeforeEach
    void setUp() {
        itemsMap = new HashMap<>();
        cacheMap = new HashMap<>();

        lenient().when(cart.getItems()).thenReturn(itemsMap);
        lenient().when(cart.getBooksCache()).thenReturn(cacheMap);
    }

    @Test
    void addBook_ShouldAddNewItem_WhenNotInCart() {
        String bookName = "Java Book";
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(bookName);
        bookDTO.setPrice(BigDecimal.TEN);

        when(bookService.getBookByName(bookName)).thenReturn(bookDTO);

        cartService.addBook(bookName);

        assertEquals(1, itemsMap.get(bookName));
        assertEquals(bookDTO, cacheMap.get(bookName));
        verify(bookService).getBookByName(bookName);
    }

    @Test
    void addBook_ShouldIncrementQuantity_WhenAlreadyInCart() {
        // Arrange
        String bookName = "Existing Book";
        itemsMap.put(bookName, 1);
        cacheMap.put(bookName, new BookDTO());

        cartService.addBook(bookName);

        assertEquals(2, itemsMap.get(bookName));
        verify(bookService, never()).getBookByName(anyString());
    }

    @Test
    void decreaseQuantity_ShouldDecrement_WhenCountGreaterThanOne() {
        String bookName = "Book x2";
        itemsMap.put(bookName, 2);

        cartService.decreaseQuantity(bookName);

        assertEquals(1, itemsMap.get(bookName));
        assertTrue(itemsMap.containsKey(bookName));
    }

    @Test
    void decreaseQuantity_ShouldRemove_WhenCountIsOne() {
        String bookName = "Book x1";
        itemsMap.put(bookName, 1);
        cacheMap.put(bookName, new BookDTO());

        cartService.decreaseQuantity(bookName);

        assertFalse(itemsMap.containsKey(bookName));
        assertFalse(cacheMap.containsKey(bookName));
    }

    @Test
    void removeItem_ShouldRemoveFromBothMaps() {
        String bookName = "To Remove";
        itemsMap.put(bookName, 5);
        cacheMap.put(bookName, new BookDTO());

        cartService.removeItem(bookName);

        assertTrue(itemsMap.isEmpty());
        assertTrue(cacheMap.isEmpty());
    }

    @Test
    void clearCart_ShouldClearMaps() {
        itemsMap.put("A", 1);
        cacheMap.put("A", new BookDTO());

        cartService.clearCart();

        assertTrue(itemsMap.isEmpty());
        assertTrue(cacheMap.isEmpty());
    }

    @Test
    void getCartDetails_ShouldReturnCombinedMap() {
        String name = "Book A";
        BookDTO dto = new BookDTO();
        dto.setName(name);

        itemsMap.put(name, 3);
        cacheMap.put(name, dto);

        Map<BookDTO, Integer> details = cartService.getCartDetails();

        assertEquals(1, details.size());
        assertEquals(3, details.get(dto));
    }

    @Test
    void getTotalPrice_ShouldCalculateCorrectly() {
        String name1 = "B1";
        BookDTO b1 = new BookDTO();
        b1.setPrice(new BigDecimal("10.00"));
        itemsMap.put(name1, 2);
        cacheMap.put(name1, b1);

        String name2 = "B2";
        BookDTO b2 = new BookDTO();
        b2.setPrice(new BigDecimal("5.00"));
        itemsMap.put(name2, 3);
        cacheMap.put(name2, b2);

        BigDecimal total = cartService.getTotalPrice();

        assertEquals(new BigDecimal("35.00"), total);
    }
}
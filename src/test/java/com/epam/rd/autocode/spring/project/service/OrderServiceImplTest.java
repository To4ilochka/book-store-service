package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getAllOrders_SortByEmployee() {
        int page = 0;
        int size = 5;
        String sortField = "employee";
        String sortDir = "asc";
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        orderService.getAllOrders(page, size, sortField, sortDir);

        verify(orderRepository).findAll(argThat((Pageable pageable) ->
                pageable.getSort().getOrderFor("employee") != null
        ));
    }

    @Test
    void getOrdersByClient_ReturnsList() {
        String email = "client@test.com";
        when(orderRepository.findAllByClientEmail(email)).thenReturn(List.of(new Order()));
        when(modelMapper.map(any(), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        List<OrderDTO> result = orderService.getOrdersByClient(email);

        assertEquals(1, result.size());
    }

    @Test
    void addOrder_Success() {
        String email = "client@test.com";
        String bookName = "Java Book";
        BigDecimal price = BigDecimal.valueOf(100);
        BigDecimal balance = BigDecimal.valueOf(200);

        BookItemDTO itemDTO = new BookItemDTO();
        itemDTO.setBookName(bookName);
        itemDTO.setQuantity(1);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(email);
        orderDTO.setPrice(price);
        orderDTO.setBookItems(List.of(itemDTO));

        Client client = new Client();
        client.setBalance(balance);

        Book book = new Book();
        book.setName(bookName);

        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findByNameIn(anySet())).thenReturn(List.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(modelMapper.map(savedOrder, OrderDTO.class)).thenReturn(orderDTO);

        OrderDTO result = orderService.addOrder(orderDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), client.getBalance());
        verify(clientRepository).save(client);
    }

    @Test
    void addOrder_InsufficientFunds_ThrowsException() {
        String email = "poor@test.com";
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(email);
        orderDTO.setPrice(BigDecimal.valueOf(100));

        Client client = new Client();
        client.setBalance(BigDecimal.valueOf(50));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        assertThrows(InsufficientFundsException.class, () -> orderService.addOrder(orderDTO));
    }

    @Test
    void addOrder_BookNotFound_ThrowsException() {
        String email = "client@test.com";
        String bookName = "Missing Book";
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(email);
        orderDTO.setPrice(BigDecimal.TEN);
        BookItemDTO item = new BookItemDTO();
        item.setBookName(bookName);
        orderDTO.setBookItems(List.of(item));

        Client client = new Client();
        client.setBalance(BigDecimal.valueOf(100));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findByNameIn(anySet())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
    }

    @Test
    void confirmOrder_Success() {
        Long orderId = 1L;
        String email = "emp@test.com";
        Order order = new Order();
        Employee employee = new Employee();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));

        orderService.confirmOrder(orderId, email);

        assertEquals(employee, order.getEmployee());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmOrder_AlreadyConfirmed_DoNothing() {
        Long orderId = 1L;
        String email = "emp@test.com";
        Order order = new Order();
        order.setEmployee(new Employee());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.confirmOrder(orderId, email);

        verify(orderRepository, never()).save(order);
    }

    @Test
    void createOrder_FromCart_DelegatesToAddOrder() {
        String email = "client@test.com";
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName("Book");
        Map<BookDTO, Integer> cart = Map.of(bookDTO, 1);
        BigDecimal total = BigDecimal.TEN;

        Client client = new Client();
        client.setBalance(new BigDecimal("100"));
        Book book = new Book();
        book.setName("Book");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findByNameIn(anySet())).thenReturn(List.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());
        when(modelMapper.map(any(), eq(OrderDTO.class))).thenReturn(new OrderDTO());

        OrderDTO result = orderService.createOrder(email, cart, total);

        assertNotNull(result);
    }
}
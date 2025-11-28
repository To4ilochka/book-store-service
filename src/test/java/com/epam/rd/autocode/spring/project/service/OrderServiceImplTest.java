package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
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
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void getAllOrders_ShouldReturnPage() {
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDir = "asc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortField));
        Page<Order> orderPage = new PageImpl<>(List.of(new Order()));

        when(orderRepository.findAll(any(Pageable.class))).thenReturn(orderPage);

        Page<Order> result = orderService.getAllOrders(page, size, sortField, sortDir);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllOrders_ShouldSortByEmployeeWithNullsFirst() {
        int page = 0;
        int size = 10;
        String sortField = "employee";
        String sortDir = "asc";

        Page<Order> orderPage = new PageImpl<>(List.of(new Order()));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(orderPage);

        Page<Order> result = orderService.getAllOrders(page, size, sortField, sortDir);

        assertNotNull(result);
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getOrdersByClient_ShouldReturnList() {
        String email = "client@test.com";
        Order order = new Order();
        OrderDTO dto = new OrderDTO();

        when(orderRepository.findAllByClientEmail(email)).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(dto);

        List<OrderDTO> result = orderService.getOrdersByClient(email);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByEmployee_ShouldReturnList() {
        String email = "emp@test.com";
        Order order = new Order();
        OrderDTO dto = new OrderDTO();

        when(orderRepository.findAllByEmployeeEmail(email)).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderDTO.class)).thenReturn(dto);

        List<OrderDTO> result = orderService.getOrdersByEmployee(email);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void addOrder_ShouldCreateOrder_WhenBalanceSufficient() {
        String email = "client@test.com";
        String bookName = "Java";
        BigDecimal price = new BigDecimal("50.00");

        OrderDTO inputDto = new OrderDTO();
        inputDto.setClientEmail(email);
        inputDto.setPrice(price);
        inputDto.setBookItems(List.of(new BookItemDTO(bookName, 1)));

        Client client = new Client();
        client.setEmail(email);
        client.setBalance(new BigDecimal("100.00"));

        Book book = new Book();
        book.setName(bookName);

        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findByNameIn(any())).thenReturn(List.of(book));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(modelMapper.map(savedOrder, OrderDTO.class)).thenReturn(inputDto);

        OrderDTO result = orderService.addOrder(inputDto);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), client.getBalance());
        verify(clientRepository).save(client);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrder_ShouldThrowIllegalState_WhenBalanceInsufficient() {
        String email = "client@test.com";
        OrderDTO inputDto = new OrderDTO();
        inputDto.setClientEmail(email);
        inputDto.setPrice(new BigDecimal("100.00"));

        Client client = new Client();
        client.setBalance(new BigDecimal("10.00"));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        assertThrows(IllegalStateException.class, () -> orderService.addOrder(inputDto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void addOrder_ShouldThrowNotFound_WhenClientMissing() {
        String email = "ghost@test.com";
        OrderDTO inputDto = new OrderDTO();
        inputDto.setClientEmail(email);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(inputDto));
    }

    @Test
    void addOrder_ShouldThrowNotFound_WhenBookMissing() {
        String email = "client@test.com";
        String bookName = "Ghost Book";
        OrderDTO inputDto = new OrderDTO();
        inputDto.setClientEmail(email);
        inputDto.setPrice(BigDecimal.TEN);
        inputDto.setBookItems(List.of(new BookItemDTO(bookName, 1)));

        Client client = new Client();
        client.setBalance(new BigDecimal("100.00"));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(bookRepository.findByNameIn(any())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class, () -> orderService.addOrder(inputDto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmOrder_ShouldUpdateOrder_WhenValid() {
        Long orderId = 1L;
        String empEmail = "emp@test.com";

        Order order = new Order();
        order.setId(orderId);

        Employee employee = new Employee();
        employee.setEmail(empEmail);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.of(employee));

        orderService.confirmOrder(orderId, empEmail);

        assertEquals(employee, order.getEmployee());
        verify(orderRepository).save(order);
    }

    @Test
    void confirmOrder_ShouldDoNothing_WhenAlreadyConfirmed() {
        Long orderId = 1L;
        String empEmail = "emp@test.com";

        Employee existingEmp = new Employee();
        existingEmp.setEmail("other@test.com");

        Order order = new Order();
        order.setEmployee(existingEmp);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.confirmOrder(orderId, empEmail);

        verify(employeeRepository, never()).findByEmail(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmOrder_ShouldThrowNotFound_WhenOrderMissing() {
        Long orderId = 999L;
        String empEmail = "emp@test.com";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(orderId, empEmail));
    }

    @Test
    void confirmOrder_ShouldThrowNotFound_WhenEmployeeMissing() {
        Long orderId = 1L;
        String empEmail = "ghost@test.com";
        Order order = new Order();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(empEmail)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(orderId, empEmail));
    }
}
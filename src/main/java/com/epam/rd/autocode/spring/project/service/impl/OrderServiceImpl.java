package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<Order> getAllOrders(int page, int size, String sortField, String sortDir) {
        log.debug("Fetching orders page: {}", page);
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort.Order order;

        if ("employee".equals(sortField)) {
            order = new Sort.Order(direction, "employee").nullsFirst();
        } else {
            order = new Sort.Order(direction, sortField);
        }

        return orderRepository.findAll(PageRequest.of(page, size, Sort.by(order)));
    }

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        log.debug("Fetching orders for client: {}", clientEmail);
        return orderRepository.findAllByClientEmail(clientEmail)
                .stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        log.debug("Fetching orders managed by employee: {}", employeeEmail);
        return orderRepository.findAllByEmployeeEmail(employeeEmail)
                .stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();
    }

    @Transactional
    @Override
    public OrderDTO addOrder(OrderDTO orderDTO) {
        String email = orderDTO.getClientEmail();
        log.info("Processing new order for client: {}", email);

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        if (client.getBalance().compareTo(orderDTO.getPrice()) < 0) {
            log.warn("Order failed: Insufficient funds. Client: {}, Balance: {}, Required: {}",
                    email, client.getBalance(), orderDTO.getPrice());
            throw new IllegalStateException("Not enough funds! Your balance: " + client.getBalance() + " $, Order total: " + orderDTO.getPrice() + " $");
        }

        client.setBalance(client.getBalance().subtract(orderDTO.getPrice()));
        clientRepository.save(client);

        Order orderEntity = new Order();
        orderEntity.setClient(client);
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setPrice(orderDTO.getPrice());

        if (orderDTO.getEmployeeEmail() != null) {
            employeeRepository.findByEmail(orderDTO.getEmployeeEmail())
                    .ifPresent(orderEntity::setEmployee);
        }

        Set<String> bookNames = orderDTO.getBookItems().stream()
                .map(BookItemDTO::getBookName)
                .collect(Collectors.toSet());

        Map<String, Book> booksMap = bookRepository.findByNameIn(bookNames).stream()
                .collect(Collectors.toMap(Book::getName, Function.identity()));

        List<BookItem> bookItems = new ArrayList<>();

        for (BookItemDTO dtoItem : orderDTO.getBookItems()) {
            String name = dtoItem.getBookName();
            Book realBook = booksMap.get(name);

            if (realBook == null) {
                log.error("Order integrity error: Book '{}' not found in DB during order creation", name);
                throw new NotFoundException("Book not found in DB: " + name);
            }

            BookItem bookItem = new BookItem();
            bookItem.setBook(realBook);
            bookItem.setQuantity(dtoItem.getQuantity());
            bookItem.setOrder(orderEntity);

            bookItems.add(bookItem);
        }

        orderEntity.setBookItems(bookItems);
        Order savedOrder = orderRepository.save(orderEntity);

        log.info("Order #{} successfully created for client: {}", savedOrder.getId(), email);
        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Transactional
    @Override
    public void confirmOrder(Long orderId, String employeeEmail) {
        log.info("Employee '{}' attempting to confirm order #{}", employeeEmail, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getEmployee() != null) {
            log.warn("Order #{} already processed by {}", orderId, order.getEmployee().getEmail());
            return;
        }

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + employeeEmail));

        order.setEmployee(employee);
        orderRepository.save(order);
        log.info("Order #{} confirmed successfully", orderId);
    }
}
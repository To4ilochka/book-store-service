package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;

import java.util.*;

public interface OrderService {

    Page<Order> getAllOrders(int page, int size, String sortField, String sortDir);

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    public void confirmOrder(Long orderId, String employeeEmail);
}

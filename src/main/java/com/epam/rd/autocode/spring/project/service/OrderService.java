package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Page<OrderDTO> getAllOrders(int page, int size, String sortField, String sortDir);

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    void confirmOrder(Long orderId, String employeeEmail);
}

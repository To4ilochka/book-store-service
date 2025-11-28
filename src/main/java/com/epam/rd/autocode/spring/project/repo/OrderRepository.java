package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByClientEmail(String email);
    List<Order> findAllByEmployeeEmail(String email);
    Page<Order> findAll(Pageable pageable);
}

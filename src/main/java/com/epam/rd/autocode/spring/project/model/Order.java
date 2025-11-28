package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "bookItems")
@EqualsAndHashCode(of = "id")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @Column(nullable = false)
    private LocalDateTime orderDate;
    @Column(nullable = false)
    private BigDecimal price;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookItem> bookItems = new ArrayList<>();
}
package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@EqualsAndHashCode(of = "id")
public class BookItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer quantity;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
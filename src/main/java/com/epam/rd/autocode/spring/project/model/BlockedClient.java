package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blocked_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;

    public BlockedClient(String email) {
        this.email = email;
    }
}

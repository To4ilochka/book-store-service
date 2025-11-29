package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clients")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Client extends User {
    @Column(nullable = false)
    private BigDecimal balance =  BigDecimal.ZERO;
    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    public Client(Long id, String email, String password, String name, BigDecimal balance) {
        super(id, email, password, name);
        this.balance = balance;
    }

    @Override
    public String getRole() {
        return Role.CLIENT.name();
    }
}
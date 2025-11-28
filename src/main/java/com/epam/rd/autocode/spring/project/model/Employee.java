package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Employee extends User {
    @Column(nullable = false)
    private LocalDate birthDate;
    @Column(nullable = false)
    private String phone;

    public Employee(Long id, String email, String password, String name, LocalDate birthDate, String phone) {
        super(id, email, password, name);
        this.birthDate = birthDate;
        this.phone = phone;
    }

    @Override
    public String getRole() {
        return Role.EMPLOYEE.name();
    }
}
package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4, message = "Password must be at least 4 characters") // Упростил для учебного проекта
    private String password;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @PositiveOrZero(message = "Balance cannot be negative")
    private BigDecimal balance;
    private boolean isBlocked;
}
package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    @NotBlank(message = "Book name cannot be empty")
    @Size(max = 100, message = "Name is too long")
    private String name;
    @NotBlank(message = "Genre cannot be empty")
    private String genre;
    @NotNull(message = "Age group is required")
    private AgeGroup ageGroup;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Publication date is required")
    @PastOrPresent(message = "Publication date cannot be in the future")
    private LocalDate publicationDate;
    @NotBlank(message = "Author cannot be empty")
    private String author;
    @NotNull(message = "Pages count is required")
    @Min(value = 1, message = "Book must have at least 1 page")
    private Integer pages;
    @NotBlank(message = "Characteristics cannot be empty")
    private String characteristics;
    @NotBlank(message = "Description cannot be empty")
    @Size(max = 2000, message = "Description is too long")
    private String description;
    @NotNull(message = "Language is required")
    private Language language;
}
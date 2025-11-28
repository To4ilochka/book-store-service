package com.epam.rd.autocode.spring.project.session;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@SessionScope
public class Cart {
    private Map<String, Integer> items = new HashMap<>();
    private Map<String, BookDTO> booksCache = new HashMap<>();
}
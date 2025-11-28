package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
    List<Book> findByNameIn(Collection<String> names);
}

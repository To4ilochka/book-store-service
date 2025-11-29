package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
    List<Book> findByNameIn(Collection<String> names);
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.genre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

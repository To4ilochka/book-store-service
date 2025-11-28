package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
    @Query("SELECT c FROM Client c ORDER BY (SELECT COUNT(b) FROM BlockedClient b WHERE b.email = c.email) DESC, c.id ASC")
    Page<Client> findAllOrderByStatusDesc(Pageable pageable);
    @Query("SELECT c FROM Client c ORDER BY (SELECT COUNT(b) FROM BlockedClient b WHERE b.email = c.email) ASC, c.id ASC")
    Page<Client> findAllOrderByStatusAsc(Pageable pageable);
}

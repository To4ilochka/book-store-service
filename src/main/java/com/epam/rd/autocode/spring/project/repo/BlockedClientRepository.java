package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.BlockedClient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedClientRepository extends JpaRepository<BlockedClient, Long> {
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}

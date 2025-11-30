package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {

    Page<ClientDTO> getAllClients(int page, int size, String sortField, String sortDir);

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClientByEmail(String email, ClientDTO client);

    void deleteClientByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    void blockClient(String email);

    void unblockClient(String email);

    List<String> getBlockedEmails();

    void topUpBalance(String email, BigDecimal amount);
}

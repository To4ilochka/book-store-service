package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<ClientDTO> getAllClients(int page, int size, String sortField, String sortDir) {
        log.debug("Fetching clients page: {}, Sort: {} {}", page, sortField, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        String actualSortField = "status".equals(sortField) ? "isBlocked" : sortField;

        return clientRepository.findAll(PageRequest.of(page, size, Sort.by(direction, actualSortField)))
                .map(client -> modelMapper.map(client, ClientDTO.class));
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        log.debug("Fetching client profile: {}", email);
        return modelMapper.map(clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Client not found: {}", email);
                    return new NotFoundException("Client not found with email: " + email);
                }), ClientDTO.class);
    }

    @Transactional
    @Override
    public ClientDTO updateClientByEmail(String email, ClientDTO client) {
        log.info("Updating client profile: {}", email);
        Client clientByEmail = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Cannot update. Client not found with email: " + email));
        modelMapper.map(client, clientByEmail);

        Client saved = clientRepository.save(clientByEmail);
        log.info("Client profile updated successfully: {}", email);
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Transactional
    @Override
    public void deleteClientByEmail(String email) {
        log.warn("Attempting to delete client: {}", email);
        if (!clientRepository.existsByEmail(email)) {
            log.error("Delete failed. Client not found: {}", email);
            throw new NotFoundException("Cannot delete. Client not found with email: " + email);
        }
        clientRepository.deleteByEmail(email);
        log.info("Client deleted: {}", email);
    }

    @Transactional
    @Override
    public ClientDTO addClient(ClientDTO clientDTO) {
        log.debug("Attempting to register client: {}", clientDTO.getEmail());

        if (clientRepository.existsByEmail(clientDTO.getEmail())
                || employeeRepository.existsByEmail(clientDTO.getEmail())) {
            log.warn("Registration failed. User with email {} already exists", clientDTO.getEmail());
            throw new AlreadyExistException("User with this email already exists");
        }

        Client saved = clientRepository.save(modelMapper.map(clientDTO, Client.class));
        log.info("Client registered successfully: {}", saved.getEmail());
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Transactional
    @Override
    public void blockClient(String email) {
        log.info("Blocking client: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        client.setBlocked(true);
        clientRepository.save(client);
    }

    @Transactional
    @Override
    public void unblockClient(String email) {
        log.info("Unblocking client: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found: " + email));

        client.setBlocked(false);
        clientRepository.save(client);
    }

    @Override
    public List<String> getBlockedEmails() {
        return clientRepository.findAll().stream()
                .filter(Client::isBlocked)
                .map(Client::getEmail)
                .toList();
    }

    @Transactional
    @Override
    public void topUpBalance(String email, BigDecimal amount) {
        log.info("Top-up request for '{}'. Amount: {}", email, amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Top-up failed. Invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be positive");
        }

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        client.setBalance(client.getBalance().add(amount));
        clientRepository.save(client);
        log.info("Balance updated for '{}'. New balance: {}", email, client.getBalance());
    }
}
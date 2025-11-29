package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void getAllClients_ShouldReturnPage() {
        int page = 0;
        int size = 5;
        String sortField = "name";
        String sortDir = "asc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortField));

        Client client = new Client();
        client.setEmail("test@test.com");
        Page<Client> clientPage = new PageImpl<>(List.of(client));
        ClientDTO clientDTO = new ClientDTO();

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

        Page<ClientDTO> result = clientService.getAllClients(page, size, sortField, sortDir);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(clientRepository).findAll(pageable);
    }

    @Test
    void getAllClients_ShouldSortByIsBlocked_WhenStatusRequested() {
        int page = 0;
        int size = 5;
        String sortField = "status";
        String sortDir = "desc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isBlocked"));

        Client client = new Client();
        Page<Client> clientPage = new PageImpl<>(List.of(client));
        ClientDTO clientDTO = new ClientDTO();

        when(clientRepository.findAll(pageable)).thenReturn(clientPage);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

        Page<ClientDTO> result = clientService.getAllClients(page, size, sortField, sortDir);

        assertNotNull(result);
        verify(clientRepository).findAll(pageable);
    }

    @Test
    void getClientByEmail_ShouldReturnClient_WhenExists() {
        String email = "test@test.com";
        Client client = new Client();
        client.setEmail(email);
        ClientDTO dto = new ClientDTO();
        dto.setEmail(email);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.getClientByEmail(email);

        assertEquals(email, result.getEmail());
    }

    @Test
    void getClientByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "missing@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));
    }

    @Test
    void addClient_ShouldSave_WhenEmailUnique() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@test.com");
        Client client = new Client();
        client.setEmail("new@test.com");
        client.setBlocked(false);
        client.setBalance(BigDecimal.ZERO);

        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(modelMapper.map(dto, Client.class)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.addClient(dto);

        assertNotNull(result);
        verify(clientRepository).save(client);
    }

    @Test
    void addClient_ShouldThrowAlreadyExist_WhenEmailExists() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@test.com");
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void updateClientByEmail_ShouldUpdate_WhenExists() {
        String email = "user@test.com";
        ClientDTO dto = new ClientDTO();
        dto.setName("New Name");

        Client existingClient = new Client();
        existingClient.setEmail(email);

        Client updatedClient = new Client();
        updatedClient.setEmail(email);
        updatedClient.setName("New Name");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(existingClient)).thenReturn(updatedClient);
        doNothing().when(modelMapper).map(dto, existingClient);
        when(modelMapper.map(updatedClient, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.updateClientByEmail(email, dto);

        assertNotNull(result);
        verify(modelMapper).map(dto, existingClient);
        verify(clientRepository).save(existingClient);
    }

    @Test
    void updateClientByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "ghost@test.com";
        ClientDTO dto = new ClientDTO();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.updateClientByEmail(email, dto));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void deleteClientByEmail_ShouldDelete_WhenExists() {
        String email = "del@test.com";
        when(clientRepository.existsByEmail(email)).thenReturn(true);

        clientService.deleteClientByEmail(email);

        verify(clientRepository).deleteByEmail(email);
    }

    @Test
    void deleteClientByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "ghost@test.com";
        when(clientRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(email));
        verify(clientRepository, never()).deleteByEmail(any());
    }

    @Test
    void blockClient_ShouldSetBlockedTrue_WhenClientExists() {
        String email = "bad@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setBlocked(false);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.blockClient(email);

        assertTrue(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void blockClient_ShouldThrowNotFound_WhenClientMissing() {
        String email = "ghost@test.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.blockClient(email));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void unblockClient_ShouldSetBlockedFalse_WhenClientExists() {
        String email = "ok@test.com";
        Client client = new Client();
        client.setEmail(email);
        client.setBlocked(true);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.unblockClient(email);

        assertFalse(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void getBlockedEmails_ShouldReturnOnlyBlockedClients() {
        Client c1 = new Client(); c1.setEmail("blocked@t.com"); c1.setBlocked(true);
        Client c2 = new Client(); c2.setEmail("active@t.com"); c2.setBlocked(false);

        when(clientRepository.findAll()).thenReturn(List.of(c1, c2));

        List<String> result = clientService.getBlockedEmails();

        assertEquals(1, result.size());
        assertTrue(result.contains("blocked@t.com"));
        assertFalse(result.contains("active@t.com"));
    }

    @Test
    void topUpBalance_ShouldIncreaseBalance_WhenAmountPositive() {
        String email = "money@test.com";
        BigDecimal amount = new BigDecimal("50.00");
        Client client = new Client();
        client.setEmail(email);
        client.setBalance(new BigDecimal("100.00"));

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.topUpBalance(email, amount);

        assertEquals(new BigDecimal("150.00"), client.getBalance());
        verify(clientRepository).save(client);
    }

    @Test
    void topUpBalance_ShouldThrow_WhenAmountNegative() {
        String email = "user@test.com";
        BigDecimal amount = new BigDecimal("-10.00");

        assertThrows(IllegalArgumentException.class, () -> clientService.topUpBalance(email, amount));
        verify(clientRepository, never()).save(any());
    }

    @Test
    void topUpBalance_ShouldThrowNotFound_WhenClientMissing() {
        String email = "missing@test.com";
        BigDecimal amount = BigDecimal.TEN;
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.topUpBalance(email, amount));
    }

    @Test
    void clientExists_ShouldReturnTrue_WhenExists() {
        String email = "exists@test.com";
        when(clientRepository.existsByEmail(email)).thenReturn(true);
        assertTrue(clientService.clientExists(email));
    }
}
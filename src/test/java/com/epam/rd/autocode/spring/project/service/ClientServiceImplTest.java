package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void getAllClients_ReturnsPage() {
        int page = 0;
        int size = 10;
        String sortField = "email";
        String sortDir = "asc";
        Client client = new Client();
        Page<Client> clientPage = new PageImpl<>(List.of(client));

        when(clientRepository.findAll(any(PageRequest.class))).thenReturn(clientPage);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(new ClientDTO());

        Page<ClientDTO> result = clientService.getAllClients(page, size, sortField, sortDir);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllClients_SortByStatus_MapsToIsBlocked() {
        int page = 0;
        int size = 10;
        String sortField = "status";
        String sortDir = "desc";

        when(clientRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        clientService.getAllClients(page, size, sortField, sortDir);

        verify(clientRepository).findAll(argThat((org.springframework.data.domain.Pageable pageable) ->
                pageable.getSort().getOrderFor("isBlocked") != null &&
                        Objects.requireNonNull(pageable.getSort().getOrderFor("isBlocked")).getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    void getClientByEmail_Found_ReturnsDTO() {
        String email = "test@email.com";
        Client client = new Client();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(new ClientDTO());

        ClientDTO result = clientService.getClientByEmail(email);

        assertNotNull(result);
    }

    @Test
    void getClientByEmail_NotFound_ThrowsException() {
        String email = "missing@email.com";
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getClientByEmail(email));
    }

    @Test
    void updateClientByEmail_Success() {
        String email = "test@email.com";
        ClientDTO dto = new ClientDTO();
        Client client = new Client();

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        doNothing().when(modelMapper).map(any(ClientDTO.class), any(Client.class));

        when(clientRepository.save(client)).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.updateClientByEmail(email, dto);

        assertNotNull(result);
    }

    @Test
    void deleteClientByEmail_Success() {
        String email = "delete@me.com";
        when(clientRepository.existsByEmail(email)).thenReturn(true);

        clientService.deleteClientByEmail(email);

        verify(clientRepository).deleteByEmail(email);
    }

    @Test
    void deleteClientByEmail_NotFound_ThrowsException() {
        String email = "delete@me.com";
        when(clientRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> clientService.deleteClientByEmail(email));
    }

    @Test
    void addClient_Success() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("new@client.com");
        Client client = new Client();

        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(modelMapper.map(dto, Client.class)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(dto);

        ClientDTO result = clientService.addClient(dto);

        assertNotNull(result);
    }

    @Test
    void addClient_ExistsInClients_ThrowsException() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("exist@client.com");
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
    }

    @Test
    void addClient_ExistsInEmployees_ThrowsException() {
        ClientDTO dto = new ClientDTO();
        dto.setEmail("staff@company.com");
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> clientService.addClient(dto));
    }

    @Test
    void blockClient_Success() {
        String email = "bad@user.com";
        Client client = new Client();
        client.setBlocked(false);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.blockClient(email);

        assertTrue(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void unblockClient_Success() {
        String email = "good@user.com";
        Client client = new Client();
        client.setBlocked(true);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.unblockClient(email);

        assertFalse(client.isBlocked());
        verify(clientRepository).save(client);
    }

    @Test
    void getBlockedEmails_ReturnsList() {
        Client c1 = new Client(); c1.setEmail("a@a.com"); c1.setBlocked(true);
        Client c2 = new Client(); c2.setEmail("b@b.com"); c2.setBlocked(false);

        when(clientRepository.findAll()).thenReturn(List.of(c1, c2));

        List<String> result = clientService.getBlockedEmails();

        assertEquals(1, result.size());
        assertEquals("a@a.com", result.get(0));
    }

    @Test
    void topUpBalance_Success() {
        String email = "rich@user.com";
        BigDecimal current = new BigDecimal("100");
        BigDecimal add = new BigDecimal("50");
        Client client = new Client();
        client.setBalance(current);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        clientService.topUpBalance(email, add);

        assertEquals(new BigDecimal("150"), client.getBalance());
        verify(clientRepository).save(client);
    }

    @Test
    void topUpBalance_NegativeAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> clientService.topUpBalance("any", new BigDecimal("-10")));
    }
}
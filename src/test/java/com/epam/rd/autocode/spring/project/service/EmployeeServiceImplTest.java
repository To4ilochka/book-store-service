package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getAllEmployees_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee employee = new Employee();
        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(employeeRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(new EmployeeDTO());

        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getEmployeeByEmail_Found_ReturnsDTO() {
        String email = "emp@test.com";
        Employee employee = new Employee();
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(new EmployeeDTO());

        EmployeeDTO result = employeeService.getEmployeeByEmail(email);

        assertNotNull(result);
    }

    @Test
    void getEmployeeByEmail_NotFound_ThrowsException() {
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail("none"));
    }

    @Test
    void updateEmployeeByEmail_Success_WithPassword() {
        String email = "emp@test.com";
        EmployeeDTO dto = new EmployeeDTO();
        dto.setPassword("newPass");
        Employee employee = new Employee();
        Employee mappedEmployee = new Employee();
        mappedEmployee.setPassword("encodedPass");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(modelMapper.map(dto, Employee.class)).thenReturn(mappedEmployee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        employeeService.updateEmployeeByEmail(email, dto);

        assertEquals("encodedPass", employee.getPassword());
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployeeByEmail_Success_WithoutPassword() {
        String email = "emp@test.com";
        EmployeeDTO dto = new EmployeeDTO();
        dto.setPassword(null);
        Employee employee = new Employee();
        employee.setPassword("oldPass");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        employeeService.updateEmployeeByEmail(email, dto);

        assertEquals("oldPass", employee.getPassword());
    }

    @Test
    void deleteEmployeeByEmail_Success() {
        String email = "del@test.com";
        when(employeeRepository.existsByEmail(email)).thenReturn(true);

        employeeService.deleteEmployeeByEmail(email);

        verify(employeeRepository).deleteByEmail(email);
    }

    @Test
    void addEmployee_Success() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@emp.com");
        Employee employee = new Employee();

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.addEmployee(dto);

        assertNotNull(result);
    }

    @Test
    void addEmployee_ExistsInClient_ThrowsException() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("client@test.com");
        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(clientRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));
    }
}
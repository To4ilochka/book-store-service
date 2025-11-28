package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void getAllEmployees_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Employee employee = new Employee();
        employee.setEmail("emp@test.com");

        Page<Employee> page = new PageImpl<>(List.of(employee));
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("emp@test.com");

        when(employeeRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        Page<EmployeeDTO> result = employeeService.getAllEmployees(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    void getEmployeeByEmail_ShouldReturnEmployee_WhenExists() {
        String email = "emp@test.com";
        Employee employee = new Employee();
        employee.setEmail(email);
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail(email);

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.getEmployeeByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getEmployeeByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "ghost@test.com";
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeByEmail(email));
    }

    @Test
    void addEmployee_ShouldSave_WhenEmailUnique() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("new@test.com");

        Employee employee = new Employee();
        employee.setEmail("new@test.com");
        employee.setId(1L);

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.addEmployee(dto);

        assertNotNull(result);
        verify(employeeRepository).save(employee);
    }

    @Test
    void addEmployee_ShouldThrowAlreadyExist_WhenEmailExists() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail("exist@test.com");

        when(employeeRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> employeeService.addEmployee(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void updateEmployeeByEmail_ShouldUpdate_WhenExists() {
        String email = "emp@test.com";
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("New Name");
        dto.setPassword("NewPass");

        Employee existing = new Employee();
        existing.setEmail(email);
        existing.setPassword("OldPass");

        Employee updated = new Employee();
        updated.setEmail(email);
        updated.setPassword("EncodedNewPass");

        Employee mappedTemp = new Employee();
        mappedTemp.setPassword("EncodedNewPass");

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(existing));
        when(modelMapper.map(dto, Employee.class)).thenReturn(mappedTemp);
        when(employeeRepository.save(existing)).thenReturn(updated);
        when(modelMapper.map(updated, EmployeeDTO.class)).thenReturn(dto);

        EmployeeDTO result = employeeService.updateEmployeeByEmail(email, dto);

        assertNotNull(result);
        verify(employeeRepository).save(existing);
    }

    @Test
    void updateEmployeeByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "ghost@test.com";
        EmployeeDTO dto = new EmployeeDTO();

        when(employeeRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.updateEmployeeByEmail(email, dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void deleteEmployeeByEmail_ShouldDelete_WhenExists() {
        String email = "del@test.com";
        when(employeeRepository.existsByEmail(email)).thenReturn(true);

        employeeService.deleteEmployeeByEmail(email);

        verify(employeeRepository).deleteByEmail(email);
    }

    @Test
    void deleteEmployeeByEmail_ShouldThrowNotFound_WhenNotExists() {
        String email = "ghost@test.com";
        when(employeeRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> employeeService.deleteEmployeeByEmail(email));
        verify(employeeRepository, never()).deleteByEmail(any());
    }

    @Test
    void employeeExists_ShouldReturnTrue_WhenExists() {
        String email = "exist@test.com";
        when(employeeRepository.existsByEmail(email)).thenReturn(true);
        assertTrue(employeeService.employeeExists(email));
    }
}
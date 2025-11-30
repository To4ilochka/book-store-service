package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        log.debug("Fetching employees page: {}", pageable.getPageNumber());
        return employeeRepository.findAll(pageable)
                .map(e -> modelMapper.map(e, EmployeeDTO.class));
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        log.debug("Fetching employee by email: {}", email);
        return employeeRepository.findByEmail(email)
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .orElseThrow(() -> {
                    log.error("Employee not found with email: {}", email);
                    return new NotFoundException("Employee not found with email: " + email);
                });
    }

    @Transactional
    @Override
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employeeDTO) {
        log.info("Updating employee with email: {}", email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));

        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setBirthDate(employeeDTO.getBirthDate());

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isBlank()) {
            String encodedPassword = modelMapper.map(employeeDTO, Employee.class).getPassword();
            employee.setPassword(encodedPassword);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", email);

        return modelMapper.map(savedEmployee, EmployeeDTO.class);
    }

    @Transactional
    @Override
    public void deleteEmployeeByEmail(String email) {
        log.warn("Attempting to delete employee with email: {}", email);

        if (!employeeRepository.existsByEmail(email)) {
            log.error("Cannot delete. Employee not found: {}", email);
            throw new NotFoundException("Employee not found with email: " + email);
        }

        employeeRepository.deleteByEmail(email);
        log.info("Employee deleted: {}", email);
    }

    @Transactional
    @Override
    public EmployeeDTO addEmployee(EmployeeDTO employeeDTO) {
        log.debug("Attempting to register employee: {}", employeeDTO.getEmail());

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())
                || clientRepository.existsByEmail(employeeDTO.getEmail())) {

            log.warn("Registration failed. Email {} is already taken", employeeDTO.getEmail());
            throw new AlreadyExistException("User with this email already exists");
        }

        Employee saved = employeeRepository.save(modelMapper.map(employeeDTO, Employee.class));
        log.info("New employee registered: {}", saved.getEmail());
        return modelMapper.map(saved, EmployeeDTO.class);
    }

    public boolean employeeExists(String email) {
        return employeeRepository.existsByEmail(email);
    }
}
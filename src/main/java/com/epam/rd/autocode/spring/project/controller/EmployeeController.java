package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public String getAllEmployees(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "id") String sort,
                                  @RequestParam(defaultValue = "asc") String dir,
                                  Model model) {
        log.debug("Requesting employee list. Page: {}, Sort: {}", page, sort);
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<EmployeeDTO> employeesPage = employeeService.getAllEmployees(pageable);
        model.addAttribute("employees", employeesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", dir.equals("asc") ? "desc" : "asc");
        return "employee/list";
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
        model.addAttribute("employee", new EmployeeDTO());
        return "employee/add";
    }

    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "employee/add";
        }

        employeeService.addEmployee(employeeDTO);

        redirectAttributes.addFlashAttribute("successMessage", "Employee added successfully");
        return "redirect:/employees";
    }

    @PostMapping("/delete")
    public String deleteEmployee(@RequestParam("email") String email, Principal principal) {
        if (principal.getName().equals(email)) {
            log.warn("SECURITY WARN: User {} tried to delete themselves!", email);
            return "redirect:/employees?error=self_delete";
        }
        log.info("Admin {} deleted employee {}", principal.getName(), email);
        employeeService.deleteEmployeeByEmail(email);
        return "redirect:/employees";
    }
}
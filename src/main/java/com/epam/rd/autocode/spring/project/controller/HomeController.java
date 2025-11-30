package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final ClientService clientService;

    @GetMapping
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String getLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String getRegisterForm(Model model) {
        model.addAttribute("user", new ClientDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") ClientDTO clientDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed for: {}", clientDTO.getEmail());
            return "auth/register";
        }

        clientService.addClient(clientDTO);

        log.info("New client registered successfully: {}", clientDTO.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please sign in.");

        return "redirect:/login";
    }
}
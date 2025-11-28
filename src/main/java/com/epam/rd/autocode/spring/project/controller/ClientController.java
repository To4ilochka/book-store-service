package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping("/client/profile")
    public String getProfile(Model model, Principal principal) {
        model.addAttribute("client", clientService.getClientByEmail(principal.getName()));
        return "client/profile";
    }

    @GetMapping("/client/profile/edit")
    public String editProfileForm(Model model, Principal principal) {
        model.addAttribute("client", clientService.getClientByEmail(principal.getName()));
        return "client/edit";
    }

    @PostMapping("/client/profile/edit")
    public String editProfile(@Valid @ModelAttribute("client") ClientDTO clientDTO,
                              BindingResult bindingResult,
                              Principal principal) {

        if (bindingResult.hasErrors()) {
            return "client/edit";
        }

        log.info("Client '{}' updated their profile info", principal.getName());
        clientService.updateClientByEmail(principal.getName(), clientDTO);
        return "redirect:/client/profile";
    }

    @PostMapping("/client/delete")
    public String deleteClient(Principal principal, HttpServletRequest request) {
        String email = principal.getName();
        log.warn("Client '{}' requested account deletion", email);

        clientService.deleteClientByEmail(email);

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        log.info("Client account '{}' successfully deleted and session invalidated", email);
        return "redirect:/login?logout";
    }

    @GetMapping("/clients")
    public String getAllClients(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "9") int size,
                                @RequestParam(defaultValue = "id") String sort,
                                @RequestParam(defaultValue = "asc") String dir,
                                Model model) {

        log.debug("Admin viewing clients list. Page: {}", page);

        Page<ClientDTO> clientsPage = clientService.getAllClients(page, size, sort, dir);

        model.addAttribute("clients", clientsPage);
        model.addAttribute("blockedEmails", clientService.getBlockedEmails());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientsPage.getTotalPages());

        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", dir.equals("asc") ? "desc" : "asc");

        return "client/list_admin";
    }

    @PostMapping("/clients/block")
    public String blockClient(@RequestParam("email") String email) {
        log.warn("Admin blocked client: {}", email);
        clientService.blockClient(email);
        return "redirect:/clients";
    }

    @PostMapping("/clients/unblock")
    public String unblockClient(@RequestParam("email") String email) {
        log.info("Admin unblocked client: {}", email);
        clientService.unblockClient(email);
        return "redirect:/clients";
    }

    @PostMapping("/client/topup")
    public String topUpBalance(@RequestParam("amount") java.math.BigDecimal amount,
                               Principal principal) {
        log.info("Client '{}' topped up balance by {}", principal.getName(), amount);
        clientService.topUpBalance(principal.getName(), amount);
        return "redirect:/client/profile";
    }
}
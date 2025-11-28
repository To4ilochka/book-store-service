package com.epam.rd.autocode.spring.project.handler;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralError(Exception ex, Model model) {
         log.error("Unexpected error occurred", ex);

        model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Exception ex, Model model) {
        model.addAttribute("errorMessage", "You do not have permission to access this page.");
        return "error/403";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDatabaseConflict(DataIntegrityViolationException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {

        if (request.getRequestURI().contains("/books/delete")) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete this book because it is part of existing orders. " +
                            "Please archive it or delete related orders first.");
            return "redirect:/books";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Database error: " + ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}

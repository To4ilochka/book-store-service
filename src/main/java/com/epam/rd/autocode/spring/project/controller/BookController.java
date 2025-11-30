package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public String getAllBooks(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "6") int size,
                              @RequestParam(defaultValue = "id") String sort,
                              @RequestParam(defaultValue = "asc") String dir,
                              @RequestParam(required = false) String keyword,
                              Model model) {

        log.debug("Fetching books catalog. Page: {}, Sort: {}, Keyword: {}", page, sort, keyword);

        Page<BookDTO> bookPage = bookService.getAllBooks(page, size, sort, dir, keyword);

        model.addAttribute("books", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());

        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", dir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);

        return "books/list";
    }

    @GetMapping("/{name}")
    public String getBook(@PathVariable String name, Model model) {
        log.debug("Viewing details for book: {}", name);
        model.addAttribute("book", bookService.getBookByName(name));
        return "books/detail";
    }

    @GetMapping("/add")
    public String createBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        return "books/add";
    }

    @PostMapping("/add")
    public String saveBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "books/add";
        }

        log.info("Adding new book: {}", bookDTO.getName());
        bookService.addBook(bookDTO);
        return "redirect:/books";
    }

    @GetMapping("/edit/{name}")
    public String editBookForm(@PathVariable String name, Model model) {
        model.addAttribute("book", bookService.getBookByName(name));
        return "books/edit";
    }

    @PostMapping("/edit/{name}")
    public String editBook(@PathVariable String name,
                           @Valid @ModelAttribute("book") BookDTO bookDTO,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "books/edit";
        }

        log.info("Updating book info: {}", name);
        bookService.updateBookByName(name, bookDTO);
        return "redirect:/books";
    }

    @PostMapping("/delete/{name}")
    public String deleteBook(@PathVariable String name) {
        log.warn("Deleting book from catalog: {}", name);
        bookService.deleteBookByName(name);
        return "redirect:/books";
    }
}
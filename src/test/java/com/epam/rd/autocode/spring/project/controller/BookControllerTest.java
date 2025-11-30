package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.security.SecurityUser;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private ClientService clientService;

    private Authentication getAuthentication(String role) {
        SecurityUser user = new SecurityUser(
                "user",
                "password",
                true,
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                "Test User"
        );
        return new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
    }

    @Test
    void getAllBooks_ShouldReturnListView() throws Exception {
        when(bookService.getAllBooks(anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/books")
                        .with(authentication(getAuthentication("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    void getBook_ShouldReturnDetailView() throws Exception {
        when(bookService.getBookByName("Java")).thenReturn(new BookDTO());

        mockMvc.perform(get("/books/Java")
                        .with(authentication(getAuthentication("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(view().name("books/detail"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void createBookForm_ShouldReturnAddView() throws Exception {
        mockMvc.perform(get("/books/add")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE"))))
                .andExpect(status().isOk())
                .andExpect(view().name("books/add"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void saveBook_Success_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE")))
                        .param("name", "New Book")
                        .param("author", "Author")
                        .param("genre", "Tech")
                        .param("price", "10.0")
                        .param("publicationDate", "2023-01-01")
                        .param("pages", "300")
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT")
                        .param("description", "Test desc")
                        .param("characteristics", "Test char"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).addBook(any(BookDTO.class));
    }

    @Test
    void editBookForm_ShouldReturnEditView() throws Exception {
        when(bookService.getBookByName("Java")).thenReturn(new BookDTO());

        mockMvc.perform(get("/books/edit/Java")
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE"))))
                .andExpect(status().isOk())
                .andExpect(view().name("books/edit"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void editBook_Success_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(post("/books/edit/Java")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE")))
                        .param("name", "Java")
                        .param("author", "Author")
                        .param("genre", "Tech")
                        .param("price", "10.0")
                        .param("publicationDate", "2023-01-01")
                        .param("pages", "300")
                        .param("language", "ENGLISH")
                        .param("ageGroup", "ADULT")
                        .param("description", "Updated desc")
                        .param("characteristics", "Updated char"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).updateBookByName(eq("Java"), any(BookDTO.class));
    }

    @Test
    void deleteBook_ShouldRedirectToBooks() throws Exception {
        mockMvc.perform(post("/books/delete/Java")
                        .with(csrf())
                        .with(authentication(getAuthentication("ROLE_EMPLOYEE"))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteBookByName("Java");
    }
}
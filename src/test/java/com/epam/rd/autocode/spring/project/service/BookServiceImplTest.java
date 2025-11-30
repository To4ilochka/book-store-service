package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
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
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void getAllBooks_SimplePageable_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Book book = new Book();
        BookDTO bookDTO = new BookDTO();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getAllBooks_WithKeyword_ReturnsFilteredPage() {
        int page = 0;
        int size = 5;
        String sort = "name";
        String direction = "asc";
        String keyword = "Java";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        Book book = new Book();
        BookDTO bookDTO = new BookDTO();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAllByKeyword(keyword, pageable)).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(page, size, sort, direction, keyword);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(bookRepository).findAllByKeyword(keyword, pageable);
    }

    @Test
    void getAllBooks_WithoutKeyword_ReturnsAll() {
        int page = 0;
        int size = 5;
        String sort = "name";
        String direction = "desc";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Book book = new Book();
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(new BookDTO());

        bookService.getAllBooks(page, size, sort, direction, null);

        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getBookByName_Exists_ReturnsDTO() {
        String name = "Test Book";
        Book book = new Book();
        book.setName(name);
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(name);

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByName(name);

        assertEquals(name, result.getName());
    }

    @Test
    void getBookByName_NotFound_ThrowsException() {
        String name = "Unknown";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookByName(name));
    }

    @Test
    void updateBookByName_Exists_ReturnsUpdatedDTO() {
        String name = "Old Name";
        BookDTO updateInfo = new BookDTO();
        updateInfo.setName(name);
        Book existingBook = new Book();
        Book savedBook = new Book();

        when(bookRepository.findByName(name)).thenReturn(Optional.of(existingBook));
        doNothing().when(modelMapper).map(any(BookDTO.class), any(Book.class));

        when(bookRepository.save(existingBook)).thenReturn(savedBook);
        when(modelMapper.map(savedBook, BookDTO.class)).thenReturn(updateInfo);

        BookDTO result = bookService.updateBookByName(name, updateInfo);

        assertNotNull(result);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBookByName_NotFound_ThrowsException() {
        String name = "Unknown";
        BookDTO dto = new BookDTO();
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBookByName(name, dto));
    }

    @Test
    void deleteBookByName_Exists_DeletesBook() {
        String name = "ToDelete";
        when(bookRepository.existsByName(name)).thenReturn(true);

        bookService.deleteBookByName(name);

        verify(bookRepository).deleteByName(name);
    }

    @Test
    void deleteBookByName_NotFound_ThrowsException() {
        String name = "Unknown";
        when(bookRepository.existsByName(name)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookService.deleteBookByName(name));
        verify(bookRepository, never()).deleteByName(anyString());
    }

    @Test
    void addBook_NewBook_ReturnsDTO() {
        BookDTO dto = new BookDTO();
        dto.setName("New Book");
        Book book = new Book();
        Book savedBook = new Book();
        savedBook.setId(1L);

        when(bookRepository.existsByName(dto.getName())).thenReturn(false);
        when(modelMapper.map(dto, Book.class)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(modelMapper.map(savedBook, BookDTO.class)).thenReturn(dto);

        BookDTO result = bookService.addBook(dto);

        assertNotNull(result);
        verify(bookRepository).save(book);
    }

    @Test
    void addBook_AlreadyExists_ThrowsException() {
        BookDTO dto = new BookDTO();
        dto.setName("Existing Book");
        when(bookRepository.existsByName(dto.getName())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(dto));
        verify(bookRepository, never()).save(any());
    }
}
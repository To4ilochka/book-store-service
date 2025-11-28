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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void getAllBooks_ShouldReturnPageOfBookDTOs() {
        Pageable pageable = PageRequest.of(0, 5);
        Book book = new Book();
        book.setName("Test Book");

        BookDTO bookDTO = new BookDTO();
        bookDTO.setName("Test Book");

        Page<Book> bookPage = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.getAllBooks(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Book", result.getContent().get(0).getName());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getBookByName_ShouldReturnBook_WhenExists() {
        String name = "Java Basics";
        Book book = new Book();
        book.setName(name);

        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(name);

        when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
        when(modelMapper.map(book, BookDTO.class)).thenReturn(bookDTO);

        BookDTO result = bookService.getBookByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    void getBookByName_ShouldThrowNotFound_WhenBookDoesNotExist() {
        String name = "Unknown Book";
        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBookByName(name));
    }

    @Test
    void addBook_ShouldSaveAndReturnBook_WhenNameIsUnique() {
        BookDTO inputDto = new BookDTO();
        inputDto.setName("New Book");
        inputDto.setPrice(BigDecimal.TEN);

        Book mappedEntity = new Book();
        mappedEntity.setName("New Book");

        Book savedEntity = new Book();
        savedEntity.setId(1L);
        savedEntity.setName("New Book");

        when(bookRepository.existsByName(inputDto.getName())).thenReturn(false);
        when(modelMapper.map(inputDto, Book.class)).thenReturn(mappedEntity);
        when(bookRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(modelMapper.map(savedEntity, BookDTO.class)).thenReturn(inputDto);

        BookDTO result = bookService.addBook(inputDto);

        assertNotNull(result);
        assertEquals("New Book", result.getName());
        verify(bookRepository).save(mappedEntity);
    }

    @Test
    void addBook_ShouldThrowAlreadyExist_WhenNameExists() {
        BookDTO inputDto = new BookDTO();
        inputDto.setName("Existing Book");

        when(bookRepository.existsByName(inputDto.getName())).thenReturn(true);

        assertThrows(AlreadyExistException.class, () -> bookService.addBook(inputDto));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBookByName_ShouldUpdateAndReturnBook_WhenExists() {
        // Arrange
        String name = "Old Name";
        BookDTO updateDto = new BookDTO();
        updateDto.setName("New Name");

        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setName(name);

        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setName("New Name");

        when(bookRepository.findByName(name)).thenReturn(Optional.of(existingBook));

        when(bookRepository.save(existingBook)).thenReturn(updatedBook);

        doNothing().when(modelMapper).map(updateDto, existingBook);

        when(modelMapper.map(updatedBook, BookDTO.class)).thenReturn(updateDto);

        BookDTO result = bookService.updateBookByName(name, updateDto);

        assertNotNull(result);
        verify(modelMapper).map(updateDto, existingBook);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBookByName_ShouldThrowNotFound_WhenBookDoesNotExist() {
        String name = "Missing Book";
        BookDTO updateDto = new BookDTO();

        when(bookRepository.findByName(name)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBookByName(name, updateDto));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBookByName_ShouldDelete_WhenExists() {
        String name = "Book To Delete";
        when(bookRepository.existsByName(name)).thenReturn(true);

        bookService.deleteBookByName(name);

        verify(bookRepository).deleteByName(name);
    }

    @Test
    void deleteBookByName_ShouldThrowNotFound_WhenBookDoesNotExist() {
        String name = "Ghost Book";
        when(bookRepository.existsByName(name)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookService.deleteBookByName(name));
        verify(bookRepository, never()).deleteByName(any());
    }
}
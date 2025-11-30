package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        log.debug("Fetching books page: {}", pageable.getPageNumber());
        return bookRepository.findAll(pageable)
                .map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookByName(String name) {
        log.debug("Fetching book details: {}", name);
        return modelMapper.map(bookRepository.findByName(name)
                .orElseThrow(() -> {
                    log.error("Book not found: {}", name);
                    return new NotFoundException("Book not found with name: " + name);
                }), BookDTO.class);
    }

    @Transactional
    @Override
    public BookDTO updateBookByName(String name, BookDTO book) {
        log.info("Updating book: {}", name);
        Book bookByName = bookRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Cannot update. Book not found with name: " + name));
        modelMapper.map(book, bookByName);

        Book saved = bookRepository.save(bookByName);
        log.info("Book updated successfully: {}", name);
        return modelMapper.map(saved,  BookDTO.class);
    }

    @Transactional
    @Override
    public void deleteBookByName(String name) {
        log.warn("Attempting to delete book: {}", name);
        if (!bookRepository.existsByName(name)) {
            log.error("Delete failed. Book not found: {}", name);
            throw new NotFoundException("Cannot delete. Book not found with name: " + name);
        }
        bookRepository.deleteByName(name);
        log.info("Book deleted: {}", name);
    }

    @Transactional
    @Override
    public BookDTO addBook(BookDTO book) {
        log.info("Adding new book: {}", book.getName());
        if (bookRepository.existsByName(book.getName())) {
            log.error("Creation failed. Book exists: {}", book.getName());
            throw new AlreadyExistException("Book with name '" + book.getName() + "' already exists");
        }
        Book saved = bookRepository.save(modelMapper.map(book, Book.class));
        log.info("Book added successfully with ID: {}", saved.getId());
        return modelMapper.map(saved,  BookDTO.class) ;
    }

    @Override
    public Page<BookDTO> getAllBooks(int page, int size, String sort, String direction, String keyword) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        log.debug("Fetching books page: {}, Keyword: {}", pageable.getPageNumber(), keyword);

        Page<Book> books;
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookRepository.findAllByKeyword(keyword.trim(), pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }

        return books.map(book -> modelMapper.map(book, BookDTO.class));
    }
}
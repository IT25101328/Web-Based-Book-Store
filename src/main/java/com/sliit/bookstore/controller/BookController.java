package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.service.AuditLogService;
import com.sliit.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/api/books/authors")
    public ResponseEntity<List<String>> getDistinctAuthors() {
        return ResponseEntity.ok(bookService.getDistinctAuthors());
    }

    @GetMapping("/api/books")
    public List<Book> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String sortBy) {
        
        auditLogService.logAction("VIEW", "Book", "Viewed list of books with advanced filters");
        return bookService.getAllBooksAdvanced(search, category, author, minPrice, maxPrice, publisher, language, year, availability, minRating, sortBy);
    }

    @GetMapping("/api/books/{id}")
    public Book getBook(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping("/api/books")
    public Book createBook(@RequestBody Book book) {
        Book created = bookService.createBook(book);
        auditLogService.logAction("ADD", "Book", "Added new book: " + book.getTitle());
        return created;
    }

    @PutMapping("/api/books/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        auditLogService.logAction("UPDATE", "Book", "Updated book ID: " + id);
        return updatedBook;
    }

    @DeleteMapping("/api/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        auditLogService.logAction("DELETE", "Book", "Deleted book ID: " + id);
        return ResponseEntity.noContent().build();
    }
}

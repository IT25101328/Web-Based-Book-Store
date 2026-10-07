package com.sliit.bookstore.service;

import com.sliit.bookstore.exception.ResourceNotFoundException;
import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.specification.BookSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<String> getDistinctAuthors() {
        return bookRepository.findDistinctAuthors();
    }

    public List<Book> getAllBooks(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return getAllBooksAdvanced(search, null, null, null, null, null, null, null, null, null, null);
        }
        return bookRepository.findAll();
    }

    public List<Book> getAllBooksAdvanced(
            String search, String category, String author,
            Double minPrice, Double maxPrice, String publisher,
            String language, Integer year, String availability, Double minRating, String sortBy) {
        
        Specification<Book> spec = BookSpecification.filterBooks(
                search, category, author, minPrice, maxPrice, publisher, language, year, availability, minRating);
        
        Sort sort = Sort.unsorted();
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                    sort = Sort.by(Sort.Direction.DESC, "id");
                    break;
                case "rating_desc":
                    sort = Sort.by(Sort.Direction.DESC, "rating");
                    break;
                case "alpha_asc":
                    sort = Sort.by(Sort.Direction.ASC, "title");
                    break;
                case "best_selling":
                    sort = Sort.by(Sort.Direction.ASC, "stock"); // Approximating high sales as low stock
                    break;
            }
        }
        
        return bookRepository.findAll(spec, sort);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book bookDetails) {
        Book book = getBookById(id);
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setGenre(bookDetails.getGenre());
        book.setIsbn(bookDetails.getIsbn());
        book.setDescription(bookDetails.getDescription());
        book.setPrice(bookDetails.getPrice());
        book.setStock(bookDetails.getStock());
        book.setCoverEmoji(bookDetails.getCoverEmoji());
        book.setCoverImage(bookDetails.getCoverImage());
        book.setPublisher(bookDetails.getPublisher());
        book.setLanguage(bookDetails.getLanguage());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setRating(bookDetails.getRating());
        book.setPageCount(bookDetails.getPageCount());
        book.setPdfFileName(bookDetails.getPdfFileName());
        book.setReleaseDate(bookDetails.getReleaseDate());
        book.setBookFormat(bookDetails.getBookFormat());
        return bookRepository.save(book);
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        
        // Manually clear dependencies to prevent Foreign Key constraint failures
        jdbcTemplate.update("DELETE FROM wishlist_items WHERE book_id = ?", id);
        jdbcTemplate.update("DELETE FROM cart_items WHERE book_id = ?", id);
        jdbcTemplate.update("UPDATE order_items SET book_id = NULL WHERE book_id = ?", id);
        
        bookRepository.delete(book);
    }

    public long countBooks() {
        return bookRepository.count();
    }
}

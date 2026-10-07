package com.sliit.bookstore.design_pattern.inventory.builder;

import com.sliit.bookstore.model.Book;
import java.math.BigDecimal;

public class BookBuilder {
    private Book book;

    public BookBuilder() {
        this.book = new Book();
    }

    public BookBuilder withTitle(String title) {
        this.book.setTitle(title);
        return this;
    }

    public BookBuilder withAuthor(String author) {
        this.book.setAuthor(author);
        return this;
    }

    public BookBuilder withIsbn(String isbn) {
        this.book.setIsbn(isbn);
        return this;
    }

    public BookBuilder withPrice(BigDecimal price) {
        this.book.setPrice(price);
        return this;
    }

    public BookBuilder withStock(Integer stock) {
        this.book.setStock(stock);
        return this;
    }

    public BookBuilder withGenre(String genre) {
        this.book.setGenre(genre);
        return this;
    }

    public Book build() {
        // Validate book object before returning if needed
        return this.book;
    }
}

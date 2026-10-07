package com.sliit.bookstore.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_collection_items")
public class BookCollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private BookCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public BookCollectionItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BookCollection getCollection() { return collection; }
    public void setCollection(BookCollection collection) { this.collection = collection; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}

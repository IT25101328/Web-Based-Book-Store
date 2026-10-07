package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.BookCollection;
import com.sliit.bookstore.model.BookCollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCollectionItemRepository extends JpaRepository<BookCollectionItem, Long> {
    List<BookCollectionItem> findByCollection(BookCollection collection);
    boolean existsByCollectionAndBookId(BookCollection collection, Long bookId);
}

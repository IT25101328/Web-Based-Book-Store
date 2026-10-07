package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    
    @Query("SELECT DISTINCT b.author FROM Book b WHERE b.author IS NOT NULL AND b.author != '' ORDER BY b.author ASC")
    List<String> findDistinctAuthors();
}

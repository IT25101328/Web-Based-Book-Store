package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.BookCollection;
import com.sliit.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCollectionRepository extends JpaRepository<BookCollection, Long> {
    List<BookCollection> findByUser(User user);
}

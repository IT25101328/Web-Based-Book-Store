package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.SavedFilter;
import com.sliit.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedFilterRepository extends JpaRepository<SavedFilter, Long> {
    List<SavedFilter> findByUser(User user);
}

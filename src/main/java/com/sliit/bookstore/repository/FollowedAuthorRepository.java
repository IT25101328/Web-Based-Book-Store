package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.FollowedAuthor;
import com.sliit.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowedAuthorRepository extends JpaRepository<FollowedAuthor, Long> {
    List<FollowedAuthor> findByUser(User user);
    boolean existsByUserAndAuthorName(User user, String authorName);
}

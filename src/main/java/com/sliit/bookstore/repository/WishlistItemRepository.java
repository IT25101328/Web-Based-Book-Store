package com.sliit.bookstore.repository;

import com.sliit.bookstore.model.Book;

import com.sliit.bookstore.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByWishlist(com.sliit.bookstore.model.Wishlist wishlist);
    Optional<WishlistItem> findByWishlistAndBook(com.sliit.bookstore.model.Wishlist wishlist, Book book);
}

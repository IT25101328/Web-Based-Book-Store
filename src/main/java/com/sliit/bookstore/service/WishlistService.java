package com.sliit.bookstore.service;

import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.model.Wishlist;
import com.sliit.bookstore.model.WishlistItem;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.repository.WishlistItemRepository;
import com.sliit.bookstore.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CartService cartService;

    @Transactional
    public Wishlist getWishlist(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return wishlistRepository.findByCustomer(user).orElseGet(() -> {
            Wishlist newWishlist = new Wishlist();
            newWishlist.setCustomer(user);
            return wishlistRepository.save(newWishlist);
        });
    }

    @Transactional
    public Wishlist addItemToWishlist(String username, Long bookId) {
        Wishlist wishlist = getWishlist(username);
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

        Optional<WishlistItem> existingItem = wishlist.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isEmpty()) {
            WishlistItem newItem = new WishlistItem();
            newItem.setWishlist(wishlist);
            newItem.setBook(book);
            wishlist.getItems().add(newItem);
        }

        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public Wishlist removeItemFromWishlist(String username, Long wishlistItemId) {
        Wishlist wishlist = getWishlist(username);
        wishlist.getItems().removeIf(item -> item.getId().equals(wishlistItemId));
        return wishlistRepository.save(wishlist);
    }

    @Transactional
    public void moveToCart(String username, Long wishlistItemId) {
        Wishlist wishlist = getWishlist(username);
        
        WishlistItem itemToMove = wishlist.getItems().stream()
                .filter(item -> item.getId().equals(wishlistItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in wishlist"));
                
        // Add to cart (default quantity 1)
        cartService.addItemToCart(username, itemToMove.getBook().getId(), 1);
        
        // Remove from wishlist
        wishlist.getItems().remove(itemToMove);
        wishlistRepository.save(wishlist);
    }
}

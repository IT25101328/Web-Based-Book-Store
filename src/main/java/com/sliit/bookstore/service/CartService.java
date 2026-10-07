package com.sliit.bookstore.service;

import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.model.Cart;
import com.sliit.bookstore.model.CartItem;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.repository.CartItemRepository;
import com.sliit.bookstore.repository.CartRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getCart(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return cartRepository.findByCustomer(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setCustomer(user);
            return cartRepository.save(newCart);
        });
    }

    @Transactional
    public Cart addItemToCart(String username, Long bookId, int quantity) {
        Cart cart = getCart(username);
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

        if (quantity > book.getStock()) {
            throw new RuntimeException("Not enough stock available");
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > book.getStock()) {
                throw new RuntimeException("Cannot add more than available stock");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setPriceAtTimeAdded(book.getPrice());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setPriceAtTimeAdded(book.getPrice());
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(String username, Long cartItemId, int quantity) {
        Cart cart = getCart(username);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        if (quantity > item.getBook().getStock()) {
            throw new RuntimeException("Not enough stock available");
        }

        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(String username, Long cartItemId) {
        Cart cart = getCart(username);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String username) {
        Cart cart = getCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}

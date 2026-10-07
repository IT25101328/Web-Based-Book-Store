package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Wishlist;
import com.sliit.bookstore.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<?> getWishlist(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(wishlistService.getWishlist(auth.getName()));
    }

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> addToWishlist(Authentication auth, @PathVariable Long bookId) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            Wishlist wishlist = wishlistService.addItemToWishlist(auth.getName(), bookId);
            return ResponseEntity.ok(Map.of("message", "Book added to wishlist successfully", "wishlist", wishlist));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{wishlistItemId}")
    public ResponseEntity<?> removeItem(Authentication auth, @PathVariable Long wishlistItemId) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(wishlistService.removeItemFromWishlist(auth.getName(), wishlistItemId));
    }

    @PostMapping("/move-to-cart/{wishlistItemId}")
    public ResponseEntity<?> moveToCart(Authentication auth, @PathVariable Long wishlistItemId) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            wishlistService.moveToCart(auth.getName(), wishlistItemId);
            return ResponseEntity.ok(Map.of("message", "Item moved to cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Cart;
import com.sliit.bookstore.model.CartItem;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.model.Wishlist;
import com.sliit.bookstore.model.WishlistItem;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.repository.WishlistItemRepository;
import com.sliit.bookstore.repository.WishlistRepository;
import com.sliit.bookstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @GetMapping
    public ResponseEntity<?> getCart(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Cart cart = cartService.getCart(auth.getName());
        
        System.out.println("=== GET /cart DEBUG LOG ===");
        System.out.println("GET /cart request received");
        System.out.println("Logged username: " + auth.getName());
        System.out.println("Retrieved cart ID: " + cart.getId());
        System.out.println("Number of cart items returned: " + cart.getItems().size());
        
        for (CartItem item : cart.getItems()) {
            System.out.println("- Book title returned: " + item.getBook().getTitle());
        }
        
        System.out.println("===========================");
        
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartCount(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Cart cart = cartService.getCart(auth.getName());
        int count = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> addToCart(Authentication auth, @PathVariable Long bookId, @RequestBody Map<String, Integer> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        int quantity = body.getOrDefault("quantity", 1);
        try {
            Cart cart = cartService.addItemToCart(auth.getName(), bookId, quantity);
            
            System.out.println("=== ADD TO CART DEBUG LOG ===");
            System.out.println("Logged username: " + auth.getName());
            System.out.println("User ID: " + user.getId());
            System.out.println("Cart ID: " + cart.getId());
            System.out.println("Book ID: " + bookId);
            
            cart.getItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .ifPresent(item -> {
                    System.out.println("CartItem ID: " + item.getId());
                    System.out.println("Quantity: " + item.getQuantity());
                });
            System.out.println("=============================");
            
            return ResponseEntity.ok(Map.of("message", "Book added to cart successfully", "cart", cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateQuantity(Authentication auth, @PathVariable Long cartItemId, @RequestBody Map<String, Integer> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        int quantity = body.getOrDefault("quantity", 1);
        try {
            Cart cart = cartService.updateItemQuantity(auth.getName(), cartItemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeItem(Authentication auth, @PathVariable Long cartItemId) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.removeItem(auth.getName(), cartItemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        cartService.clearCart(auth.getName());
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }

    @PostMapping("/move-to-wishlist/{cartItemId}")
    public ResponseEntity<?> moveToWishlist(Authentication auth, @PathVariable Long cartItemId) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Cart cart = cartService.getCart(auth.getName());
        
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem cartItem = itemOpt.get();
            
            // Add to wishlist
            Wishlist wishlist = wishlistRepository.findByCustomer(user).orElseGet(() -> {
                Wishlist w = new Wishlist();
                w.setCustomer(user);
                return wishlistRepository.save(w);
            });

            if (wishlistItemRepository.findByWishlistAndBook(wishlist, cartItem.getBook()).isEmpty()) {
                WishlistItem wlItem = new WishlistItem();
                wlItem.setWishlist(wishlist);
                wlItem.setBook(cartItem.getBook());
                wishlistItemRepository.save(wlItem);
            }
            
            // Remove from cart
            cartService.removeItem(auth.getName(), cartItemId);
            
            return ResponseEntity.ok(Map.of("message", "Book moved to wishlist"));
        }
        
        return ResponseEntity.badRequest().body(Map.of("message", "Item not found in cart"));
    }
}

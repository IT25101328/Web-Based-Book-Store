package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.model.WishlistItem;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.repository.WishlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.sliit.bookstore.security.JwtUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishlistItemRepository wishlistRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "address", user.getAddress() != null ? user.getAddress() : "",
                "role", user.getRole(),
                "profilePicture", user.getProfilePicture() != null ? user.getProfilePicture() : ""
        ));
    }

    @Autowired
    private JwtUtil jwtUtil;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody Map<String, String> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        boolean usernameChanged = false;

        if (body.containsKey("username") && !body.get("username").isBlank()) {
            String newUsername = body.get("username").trim();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken"));
                }
                user.setUsername(newUsername);
                usernameChanged = true;
            }
        }

        if (body.containsKey("email") && !body.get("email").isBlank()) {
            String newEmail = body.get("email").trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Email is already in use"));
                }
                user.setEmail(newEmail);
            }
        }

        if (body.containsKey("address")) {
            user.setAddress(body.get("address"));
        }
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        if (usernameChanged) {
            String newToken = jwtUtil.generateTokenFromUsername(user.getUsername());
            response.put("token", newToken);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/picture")
    public ResponseEntity<?> uploadProfilePicture(Authentication auth, @RequestParam("file") MultipartFile file) {
        if (auth == null) return ResponseEntity.status(401).build();
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Please select a file to upload."));

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp") && !contentType.equals("image/gif"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only image files (JPG/PNG/WEBP/GIF) are allowed."));
        }

        try {
            Path uploadPath = Paths.get("uploads/profiles/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            user.setProfilePicture(uniqueFileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture updated successfully",
                    "profilePicture", uniqueFileName
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload image: " + e.getMessage()));
        }
    }

    @Autowired
    private com.sliit.bookstore.repository.WishlistRepository newWishlistRepo;

    // WISHLIST ENDPOINTS
    @GetMapping("/wishlist")
    public ResponseEntity<?> getWishlist(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        com.sliit.bookstore.model.Wishlist wishlist = newWishlistRepo.findByCustomer(user).orElse(null);
        if (wishlist == null) return ResponseEntity.ok(List.of());
        
        List<Book> books = wishlistRepository.findByWishlist(wishlist).stream()
                .map(item -> item.getBook())
                .collect(Collectors.toList());
        return ResponseEntity.ok(books);
    }

    @PostMapping("/wishlist/{bookId}")
    public ResponseEntity<?> addToWishlist(Authentication auth, @PathVariable Long bookId) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Book book = bookRepository.findById(bookId).orElse(null);
        
        if (book == null) return ResponseEntity.notFound().build();

        com.sliit.bookstore.model.Wishlist wishlist = newWishlistRepo.findByCustomer(user).orElseGet(() -> {
            com.sliit.bookstore.model.Wishlist w = new com.sliit.bookstore.model.Wishlist();
            w.setCustomer(user);
            return newWishlistRepo.save(w);
        });

        if (wishlistRepository.findByWishlistAndBook(wishlist, book).isEmpty()) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(wishlist);
            item.setBook(book);
            wishlistRepository.save(item);
        }
        return ResponseEntity.ok(Map.of("message", "Added to wishlist"));
    }

    @DeleteMapping("/wishlist/{bookId}")
    public ResponseEntity<?> removeFromWishlist(Authentication auth, @PathVariable Long bookId) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        Book book = bookRepository.findById(bookId).orElse(null);

        if (book != null) {
            newWishlistRepo.findByCustomer(user).ifPresent(wishlist -> {
                wishlistRepository.findByWishlistAndBook(wishlist, book)
                        .ifPresent(wishlistRepository::delete);
            });
        }
        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        if (auth == null) return ResponseEntity.status(401).build();
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null || newPassword == null || currentPassword.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Both current and new password are required"));
        }
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("message", "Current password is incorrect"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}

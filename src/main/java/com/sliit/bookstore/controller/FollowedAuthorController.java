package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.FollowedAuthor;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.FollowedAuthorRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/followed-authors")
public class FollowedAuthorController {

    @Autowired
    private FollowedAuthorRepository repository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getFollowedAuthors(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        return ResponseEntity.ok(repository.findByUser(user));
    }

    @PostMapping
    public ResponseEntity<?> followAuthor(Authentication auth, @RequestBody Map<String, String> payload) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        String authorName = payload.get("authorName");
        if (authorName == null || authorName.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "Author name required"));

        if (!repository.existsByUserAndAuthorName(user, authorName)) {
            FollowedAuthor followed = new FollowedAuthor();
            followed.setUser(user);
            followed.setAuthorName(authorName);
            repository.save(followed);
        }

        return ResponseEntity.ok(Map.of("message", "Successfully followed author"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> unfollowAuthor(Authentication auth, @PathVariable Long id) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        FollowedAuthor existing = repository.findById(id).orElse(null);
        if (existing != null && existing.getUser().getId().equals(user.getId())) {
            repository.delete(existing);
            return ResponseEntity.ok(Map.of("message", "Unfollowed author"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Not found"));
    }
}

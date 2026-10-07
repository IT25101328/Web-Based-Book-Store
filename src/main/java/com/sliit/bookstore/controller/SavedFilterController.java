package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.SavedFilter;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.SavedFilterRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-filters")
public class SavedFilterController {

    @Autowired
    private SavedFilterRepository filterRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getFilters(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();
        List<SavedFilter> filters = filterRepository.findByUser(user);
        return ResponseEntity.ok(filters);
    }

    @PostMapping
    public ResponseEntity<?> createFilter(Authentication auth, @RequestBody SavedFilter filterData) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();
        
        filterData.setUser(user);
        SavedFilter saved = filterRepository.save(filterData);
        return ResponseEntity.ok(Map.of("message", "Filter saved successfully", "filter", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFilter(Authentication auth, @PathVariable Long id, @RequestBody SavedFilter filterData) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        SavedFilter existing = filterRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(404).body(Map.of("message", "Filter not found"));
        }

        existing.setName(filterData.getName());
        existing.setSearchQuery(filterData.getSearchQuery());
        existing.setCategories(filterData.getCategories());
        existing.setMinPrice(filterData.getMinPrice());
        existing.setMaxPrice(filterData.getMaxPrice());
        existing.setLanguage(filterData.getLanguage());
        existing.setMinRating(filterData.getMinRating());
        existing.setAvailability(filterData.getAvailability());

        SavedFilter saved = filterRepository.save(existing);
        return ResponseEntity.ok(Map.of("message", "Filter updated successfully", "filter", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFilter(Authentication auth, @PathVariable Long id) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        SavedFilter existing = filterRepository.findById(id).orElse(null);
        if (existing != null && existing.getUser().getId().equals(user.getId())) {
            filterRepository.delete(existing);
            return ResponseEntity.ok(Map.of("message", "Filter deleted successfully"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Filter not found"));
    }
}

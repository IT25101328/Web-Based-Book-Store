package com.sliit.bookstore.controller;

import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.model.BookCollection;
import com.sliit.bookstore.model.BookCollectionItem;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.repository.BookCollectionItemRepository;
import com.sliit.bookstore.repository.BookCollectionRepository;
import com.sliit.bookstore.repository.BookRepository;
import com.sliit.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
public class BookCollectionController {

    @Autowired
    private BookCollectionRepository collectionRepository;

    @Autowired
    private BookCollectionItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<?> getCollections(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        List<BookCollection> collections = collectionRepository.findByUser(user);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (BookCollection c : collections) {
            List<BookCollectionItem> items = itemRepository.findByCollection(c);
            List<Book> books = items.stream().map(item -> item.getBook()).toList();
            result.add(Map.of("id", c.getId(), "name", c.getName(), "books", books));
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createCollection(Authentication auth, @RequestBody Map<String, String> payload) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        String name = payload.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().body(Map.of("message", "Name required"));

        BookCollection collection = new BookCollection();
        collection.setUser(user);
        collection.setName(name);
        collection = collectionRepository.save(collection);

        return ResponseEntity.ok(Map.of("message", "Collection created", "id", collection.getId()));
    }

    @PostMapping("/{collectionId}/add/{bookId}")
    public ResponseEntity<?> addBookToCollection(Authentication auth, @PathVariable Long collectionId, @PathVariable Long bookId) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        BookCollection collection = collectionRepository.findById(collectionId).orElse(null);
        if (collection == null || !collection.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(404).body(Map.of("message", "Collection not found"));
        }

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return ResponseEntity.status(404).body(Map.of("message", "Book not found"));

        if (!itemRepository.existsByCollectionAndBookId(collection, bookId)) {
            BookCollectionItem item = new BookCollectionItem();
            item.setCollection(collection);
            item.setBook(book);
            itemRepository.save(item);
        }

        return ResponseEntity.ok(Map.of("message", "Book added to collection"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(Authentication auth, @PathVariable Long id) {
        if (auth == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(404).build();

        BookCollection collection = collectionRepository.findById(id).orElse(null);
        if (collection != null && collection.getUser().getId().equals(user.getId())) {
            List<BookCollectionItem> items = itemRepository.findByCollection(collection);
            itemRepository.deleteAll(items);
            collectionRepository.delete(collection);
            return ResponseEntity.ok(Map.of("message", "Collection deleted"));
        }
        return ResponseEntity.status(404).body(Map.of("message", "Not found"));
    }
}

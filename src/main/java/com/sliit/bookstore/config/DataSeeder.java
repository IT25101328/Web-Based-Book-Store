package com.sliit.bookstore.config;

import com.sliit.bookstore.model.Role;
import com.sliit.bookstore.model.User;
import com.sliit.bookstore.model.Book;
import com.sliit.bookstore.repository.UserRepository;
import com.sliit.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookRepository bookRepository;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@starlight.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✅ DataSeeder: Admin user created automatically (admin / admin)");
        }
        
        List<Book> books = bookRepository.findAll();
        if (books.isEmpty()) {
            // Disabled automatic seeding of dummy books upon user request
            // so that all stats start at 0 until the user manually adds them.
            System.out.println("✅ DataSeeder: Books table is empty. Automatic seeding disabled.");
        } else {
            // Check if existing books need updates (for older seeded data)
            boolean updated = false;
            java.util.Random random = new java.util.Random();
            String[] publishers = {"Penguin Classics", "HarperCollins", "Vintage", "O'Reilly Media", "Simon & Schuster"};
            String[] languages = {"English", "English", "Sinhala", "Tamil"};
            
            for (Book book : books) {
                if (book.getPublisher() == null) {
                    book.setPublisher(publishers[random.nextInt(publishers.length)]);
                    updated = true;
                }
                if (book.getLanguage() == null) {
                    book.setLanguage(languages[random.nextInt(languages.length)]);
                    updated = true;
                }
                if (book.getPublicationYear() == null) {
                    book.setPublicationYear(2010 + random.nextInt(15));
                    updated = true;
                }
                if (book.getRating() == null) {
                    double r = 3.5 + (random.nextDouble() * 1.5);
                    book.setRating(Math.round(r * 10.0) / 10.0);
                    updated = true;
                }
            }
            if (updated) {
                bookRepository.saveAll(books);
                System.out.println("✅ DataSeeder: Updated existing books with new catalog fields.");
            }
        }
    }
}

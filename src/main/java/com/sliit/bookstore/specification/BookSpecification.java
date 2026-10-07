package com.sliit.bookstore.specification;

import com.sliit.bookstore.model.Book;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBooks(
            String search, String category, String author,
            Double minPrice, Double maxPrice, String publisher,
            String language, Integer year, String availability, Double minRating) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate titlePred = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
                Predicate authorPred = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), pattern);
                Predicate genrePred = criteriaBuilder.like(criteriaBuilder.lower(root.get("genre")), pattern);
                Predicate isbnPred = criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), pattern);
                Predicate descPred = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                predicates.add(criteriaBuilder.or(titlePred, authorPred, genrePred, isbnPred, descPred));
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("genre")), "%" + category.toLowerCase() + "%"));
            }

            if (author != null && !author.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (publisher != null && !publisher.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("publisher")), "%" + publisher.toLowerCase() + "%"));
            }

            if (language != null && !language.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("language")), language.toLowerCase()));
            }

            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("publicationYear"), year));
            }

            if (availability != null && !availability.trim().isEmpty()) {
                if (availability.equalsIgnoreCase("In Stock")) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));
                } else if (availability.equalsIgnoreCase("Out of Stock")) {
                    predicates.add(criteriaBuilder.equal(root.get("stock"), 0));
                }
            }

            if (minRating != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

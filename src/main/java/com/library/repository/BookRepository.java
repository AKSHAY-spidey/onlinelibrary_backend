package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByCategoryContainingIgnoreCase(String category);

    @Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
    List<Book> findAvailableBooks();

    List<Book> findByAvailableCopiesGreaterThan(Integer copies);

    boolean existsByIsbn(String isbn);

    Book findByTitleAndAuthor(String title, String author);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) = LOWER(:title) AND LOWER(b.author) = LOWER(:author)")
    Book findExactMatch(@Param("title") String title, @Param("author") String author);

    @Query("SELECT b FROM Book b WHERE b.language = :language")
    List<Book> findByLanguage(@Param("language") String language);

    @Query("SELECT b FROM Book b WHERE b.publisher = :publisher")
    List<Book> findByPublisher(@Param("publisher") String publisher);

    @Query("SELECT DISTINCT b.language FROM Book b WHERE b.language IS NOT NULL")
    List<String> findAllLanguages();

    @Query("SELECT DISTINCT b.publisher FROM Book b WHERE b.publisher IS NOT NULL")
    List<String> findAllPublishers();

    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')))")
    List<Book> searchBooks(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category);

    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:category IS NULL OR LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
           "(:publisher IS NULL OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
           "(:language IS NULL OR LOWER(b.language) LIKE LOWER(CONCAT('%', :language, '%'))) AND " +
           "(:minYear IS NULL OR FUNCTION('YEAR', b.publicationDate) >= :minYear) AND " +
           "(:maxYear IS NULL OR FUNCTION('YEAR', b.publicationDate) <= :maxYear)")
    List<Book> advancedSearch(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category,
            @Param("publisher") String publisher,
            @Param("language") String language,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear);

    @Query("SELECT b FROM Book b ORDER BY b.id DESC")
    List<Book> findRecentlyAdded(org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT * FROM books ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Book> findRandomBooks(@Param("limit") int limit);
}

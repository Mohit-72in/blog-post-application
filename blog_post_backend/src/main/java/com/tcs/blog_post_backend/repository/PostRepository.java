package com.tcs.blog_post_backend.repository;

import com.tcs.blog_post_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(String author);

    boolean existsByAuthorIgnoreCaseAndTitleIgnoreCase(String author, String title);
    boolean existsByAuthorIgnoreCaseAndTitleIgnoreCaseAndIdNot(String author, String title, Long id);

    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
            "(:author IS NULL OR :author = '' OR LOWER(p.author) = LOWER(:author)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.body) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.author) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Post p LEFT JOIN p.tags t WHERE " +
            "(:author IS NULL OR :author = '' OR LOWER(p.author) = LOWER(:author)) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.body) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.author) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> searchPosts(@Param("author") String author, @Param("search") String search, Pageable pageable);
}

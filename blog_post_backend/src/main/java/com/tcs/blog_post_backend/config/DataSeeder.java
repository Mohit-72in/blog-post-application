package com.tcs.blog_post_backend.config;

import com.tcs.blog_post_backend.model.Post;
import com.tcs.blog_post_backend.model.User;
import com.tcs.blog_post_backend.repository.PostRepository;
import com.tcs.blog_post_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(PostRepository postRepository, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Ensure AUTHOR column exists in posts table for existing databases
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS author VARCHAR(50) DEFAULT 'anonymous'");
            jdbcTemplate.execute("UPDATE posts SET author = 'anonymous' WHERE author IS NULL OR author = ''");
        } catch (Exception e) {
            // Table might not exist yet before Hibernate initializes it
        }

        if (userRepository.count() == 0) {
            userRepository.saveAll(Arrays.asList(
                new User("john_doe", "John Doe"),
                new User("jane_smith", "Jane Smith"),
                new User("tech_guru", "Tech Guru")
            ));
        }

        if (postRepository.count() == 0) {
            Post post1 = new Post(
                    "His mother had always taught him",
                    "His mother had always taught him not to ever think of himself as better than others. He'd tried to live by this motto. He never looked down on those who were less fortunate or who had less money than him. But the stupidity of the group of people he was talking to made him change his mind.",
                    Arrays.asList("history", "american", "crime"),
                    "john_doe"
            );
            post1.setReactions(192);
            post1.setCreatedAt(LocalDateTime.now().minusHours(5));

            Post post2 = new Post(
                    "He was an expert but not in a discipline",
                    "He was an expert but not in a discipline that anyone could fully appreciate. He knew how to hold the cone just right so that the soft server ice-cream fell into it at the precise angle to form a perfect cone each and every time. It had taken years to perfect and he could now do it without even putting any thought behind it.",
                    Arrays.asList("french", "fiction", "english"),
                    "jane_smith"
            );
            post2.setReactions(45);
            post2.setCreatedAt(LocalDateTime.now().minusHours(4));

            Post post3 = new Post(
                    "Exploring the Wonders of Modern Web Development",
                    "Modern web development has evolved significantly with component-based architectures, standalone components, dynamic reactive state management, and optimized REST APIs. Building full-stack applications with clean architectural patterns provides seamless developer experience and exceptional end-user value.",
                    Arrays.asList("tech", "webdev", "angular"),
                    "tech_guru"
            );
            post3.setReactions(88);
            post3.setCreatedAt(LocalDateTime.now().minusHours(3));

            Post post4 = new Post(
                    "The Journey to Mastering Java and Spring Boot",
                    "Spring Boot simplifies microservices and enterprise Java application development. With dependency injection, Spring Data JPA, automated transactions, and concise REST controllers, developer productivity reaches new heights without sacrificing maintainability.",
                    Arrays.asList("java", "springboot", "backend"),
                    "john_doe"
            );
            post4.setReactions(120);
            post4.setCreatedAt(LocalDateTime.now().minusHours(2));

            Post post5 = new Post(
                    "Designing User Interfaces That Delight",
                    "A visually engaging user interface combines aesthetic elegance, smooth dynamic transitions, and responsive layout structures. Micro-interactions and harmonious color schemes create a memorable first impression for every user.",
                    Arrays.asList("design", "ui", "ux"),
                    "jane_smith"
            );
            post5.setReactions(64);
            post5.setCreatedAt(LocalDateTime.now().minusHours(1));

            postRepository.saveAll(Arrays.asList(post1, post2, post3, post4, post5));
        }
    }
}

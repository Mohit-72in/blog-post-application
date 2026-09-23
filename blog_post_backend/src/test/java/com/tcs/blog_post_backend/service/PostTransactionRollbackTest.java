package com.tcs.blog_post_backend.service;

import com.tcs.blog_post_backend.dto.CreatePostRequest;
import com.tcs.blog_post_backend.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PostTransactionRollbackTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    void testCreatePostWithInvalidFirstCommentRollback() {
        long initialCount = postRepository.count();

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Rollback Test Title");
        request.setBody("Rollback Test Body Content");
        request.setTags(Arrays.asList("test", "rollback"));
        request.setFirstComment(""); // Invalid comment (blank)

        // Verify exception is thrown when attempting to create post with invalid first comment
        assertThrows(IllegalArgumentException.class, () -> {
            postService.createPost(request);
        });

        long finalCount = postRepository.count();

        // Assert post count is unchanged, proving that the transaction was rolled back
        assertEquals(initialCount, finalCount, "Post count should remain unchanged after failed create post transaction");
    }
}

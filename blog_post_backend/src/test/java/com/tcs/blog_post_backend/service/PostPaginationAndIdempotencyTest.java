package com.tcs.blog_post_backend.service;

import com.tcs.blog_post_backend.dto.CreatePostRequest;
import com.tcs.blog_post_backend.dto.PagedResponse;
import com.tcs.blog_post_backend.dto.UpdatePostRequest;
import com.tcs.blog_post_backend.exception.BusinessException;
import com.tcs.blog_post_backend.model.Post;
import com.tcs.blog_post_backend.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostPaginationAndIdempotencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    void testDuplicatePostThrowsIdempotencyException() {
        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("Unique Post Title 101");
        request1.setBody("This is the first post body.");
        request1.setAuthor("test_user");
        request1.setTags(List.of("testing"));

        Post created = postService.createPost(request1);
        assertNotNull(created.getId());

        // Attempting to post the same post with same title and same author
        CreatePostRequest duplicateRequest = new CreatePostRequest();
        duplicateRequest.setTitle("Unique Post Title 101");
        duplicateRequest.setBody("Different body or duplicate retry");
        duplicateRequest.setAuthor("test_user");
        duplicateRequest.setTags(List.of("testing"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.createPost(duplicateRequest);
        });

        assertTrue(exception.getMessage().contains("already been posted"));
    }

    @Test
    void testDifferentUserCanPostWithSameTitle() {
        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("Shared Topic Title");
        request1.setBody("Author A thoughts");
        request1.setAuthor("user_a");
        request1.setTags(List.of("general"));

        Post postA = postService.createPost(request1);
        assertNotNull(postA.getId());

        CreatePostRequest request2 = new CreatePostRequest();
        request2.setTitle("Shared Topic Title");
        request2.setBody("Author B thoughts");
        request2.setAuthor("user_b");
        request2.setTags(List.of("general"));

        Post postB = postService.createPost(request2);
        assertNotNull(postB.getId());
        assertNotEquals(postA.getId(), postB.getId());
    }

    @Test
    void testPaginationReturnsCorrectPages() {
        PagedResponse<Post> page0 = postService.getPosts(null, null, 0, 2);
        assertNotNull(page0);
        assertTrue(page0.getContent().size() <= 2);
        assertEquals(0, page0.getPageNumber());
        assertEquals(2, page0.getPageSize());
        assertTrue(page0.getTotalElements() >= 2);
    }

    @Test
    void testSearchByKeyword() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Quantum Computing Breakthroughs");
        request.setBody("Deep dive into qubits and quantum entanglement.");
        request.setAuthor("physicist_alice");
        request.setTags(Arrays.asList("quantum", "science"));

        postService.createPost(request);

        PagedResponse<Post> searchResults = postService.getPosts(null, "quantum", 0, 10);
        assertNotNull(searchResults);
        assertFalse(searchResults.getContent().isEmpty());
        assertTrue(searchResults.getContent().stream().anyMatch(p -> p.getTitle().contains("Quantum")));
    }

    @Test
    void testUpdateDuplicateTitleThrowsException() {
        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("Original Article One");
        request1.setBody("Body 1");
        request1.setAuthor("author_x");
        Post post1 = postService.createPost(request1);

        CreatePostRequest request2 = new CreatePostRequest();
        request2.setTitle("Original Article Two");
        request2.setBody("Body 2");
        request2.setAuthor("author_x");
        Post post2 = postService.createPost(request2);

        // Try to update post2 to have the title of post1
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setTitle("Original Article One");
        updateRequest.setBody("Updated body");
        updateRequest.setTags(List.of("tech"));

        assertThrows(BusinessException.class, () -> {
            postService.updatePost(post2.getId(), updateRequest);
        });
    }
}

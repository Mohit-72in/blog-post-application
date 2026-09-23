package com.tcs.blog_post_backend.controller;

import com.tcs.blog_post_backend.dto.CreateCommentRequest;
import com.tcs.blog_post_backend.dto.CreatePostRequest;
import com.tcs.blog_post_backend.dto.UpdatePostRequest;
import com.tcs.blog_post_backend.model.Comment;
import com.tcs.blog_post_backend.model.Post;
import com.tcs.blog_post_backend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts(@RequestParam(required = false) String author) {
        return ResponseEntity.ok(postService.getAllPosts(author));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        Post createdPost = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        Post updatedPost = postService.updatePost(id, request);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/react")
    public ResponseEntity<Post> reactToPost(@PathVariable Long id) {
        Post updatedPost = postService.reactToPost(id);
        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getCommentsByPostId(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest request) {
        Comment createdComment = postService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}

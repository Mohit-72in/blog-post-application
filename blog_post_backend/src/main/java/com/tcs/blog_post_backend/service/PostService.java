package com.tcs.blog_post_backend.service;

import com.tcs.blog_post_backend.dto.CreateCommentRequest;
import com.tcs.blog_post_backend.dto.CreatePostRequest;
import com.tcs.blog_post_backend.dto.UpdatePostRequest;
import com.tcs.blog_post_backend.exception.ResourceNotFoundException;
import com.tcs.blog_post_backend.model.Comment;
import com.tcs.blog_post_backend.model.Post;
import com.tcs.blog_post_backend.repository.CommentRepository;
import com.tcs.blog_post_backend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts(String author) {
        if (author != null && !author.isBlank()) {
            return postRepository.findByAuthorOrderByCreatedAtDesc(author.trim().toLowerCase());
        }
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    @Transactional
    public Post createPost(CreatePostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        post.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        post.setAuthor(request.getAuthor() != null && !request.getAuthor().isBlank() ? request.getAuthor().trim().toLowerCase() : "anonymous");
        post.setReactions(0);
        post.setCreatedAt(LocalDateTime.now());

        // Save post first to demonstrate transaction rollback if comment fails
        Post savedPost = postRepository.save(post);

        if (request.getFirstComment() != null) {
            String commentText = request.getFirstComment();
            if (commentText.isBlank() || commentText.length() > 500) {
                throw new IllegalArgumentException("Invalid first comment: text cannot be blank or exceed 500 characters");
            }
            Comment comment = new Comment(commentText, savedPost);
            commentRepository.save(comment);
        }

        return savedPost;
    }

    @Transactional
    public Post updatePost(Long id, UpdatePostRequest request) {
        Post post = getPostById(id);
        post.setTitle(request.getTitle());
        post.setBody(request.getBody());
        post.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }

    @Transactional
    public Post reactToPost(Long id) {
        Post post = getPostById(id);
        post.setReactions(post.getReactions() + 1);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        // Ensure post exists
        getPostById(postId);
        return commentRepository.findByPostId(postId);
    }

    @Transactional
    public Comment addComment(Long postId, CreateCommentRequest request) {
        Post post = getPostById(postId);
        Comment comment = new Comment(request.getText(), post);
        return commentRepository.save(comment);
    }
}

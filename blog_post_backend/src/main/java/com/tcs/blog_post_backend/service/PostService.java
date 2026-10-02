package com.tcs.blog_post_backend.service;

import com.tcs.blog_post_backend.dto.CreateCommentRequest;
import com.tcs.blog_post_backend.dto.CreatePostRequest;
import com.tcs.blog_post_backend.dto.PagedResponse;
import com.tcs.blog_post_backend.dto.UpdatePostRequest;
import com.tcs.blog_post_backend.exception.BusinessException;
import com.tcs.blog_post_backend.exception.ResourceNotFoundException;
import com.tcs.blog_post_backend.model.Comment;
import com.tcs.blog_post_backend.model.Post;
import com.tcs.blog_post_backend.repository.CommentRepository;
import com.tcs.blog_post_backend.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public PagedResponse<Post> getPosts(String author, String search, int page, int size) {
        String normalizedAuthor = (author != null && !author.isBlank()) ? author.trim().toLowerCase() : null;
        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        int validatedPage = Math.max(0, page);
        int validatedSize = Math.max(1, Math.min(size, 50));

        Pageable pageable = PageRequest.of(validatedPage, validatedSize, Sort.by("createdAt").descending());
        Page<Post> pageResult = postRepository.searchPosts(normalizedAuthor, normalizedSearch, pageable);

        return new PagedResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
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
        String author = (request.getAuthor() != null && !request.getAuthor().isBlank())
                ? request.getAuthor().trim().toLowerCase()
                : "anonymous";
        String title = request.getTitle() != null ? request.getTitle().trim() : "";

        // Idempotency check: Verify user has not already published a post with identical title
        if (postRepository.existsByAuthorIgnoreCaseAndTitleIgnoreCase(author, title)) {
            throw new BusinessException("A post with title '" + title + "' has already been posted by @" + author + ". Duplicate posts are not allowed.");
        }

        Post post = new Post();
        post.setTitle(title);
        post.setBody(request.getBody());
        post.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        post.setAuthor(author);
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
        String newTitle = request.getTitle() != null ? request.getTitle().trim() : "";

        // Check if updating to a title already used in another post by this author
        if (!newTitle.equalsIgnoreCase(post.getTitle().trim()) &&
                postRepository.existsByAuthorIgnoreCaseAndTitleIgnoreCaseAndIdNot(post.getAuthor(), newTitle, id)) {
            throw new BusinessException("A post with title '" + newTitle + "' already exists for @" + post.getAuthor() + ".");
        }

        post.setTitle(newTitle);
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

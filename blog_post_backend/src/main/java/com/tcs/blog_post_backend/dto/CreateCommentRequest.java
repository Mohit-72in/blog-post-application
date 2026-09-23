package com.tcs.blog_post_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 500, message = "Comment text must not exceed 500 characters")
    private String text;

    public CreateCommentRequest() {}

    public CreateCommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

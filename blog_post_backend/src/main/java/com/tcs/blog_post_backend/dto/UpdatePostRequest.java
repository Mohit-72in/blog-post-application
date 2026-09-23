package com.tcs.blog_post_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Body is required")
    @Size(max = 2000, message = "Body must not exceed 2000 characters")
    private String body;

    private List<String> tags;

    public UpdatePostRequest() {}

    public UpdatePostRequest(String title, String body, List<String> tags) {
        this.title = title;
        this.body = body;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

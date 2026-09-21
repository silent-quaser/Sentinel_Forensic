package com.sentinelforensic.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateNoteRequest {
    private String author;

    @NotBlank(message = "Note content cannot be blank")
    private String content;

    public CreateNoteRequest() {}

    public CreateNoteRequest(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

package com.project.feedme.controllers;

import com.project.feedme.dtos.CreatePostRequest;
import com.project.feedme.dtos.PostResponse;
import com.project.feedme.services.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final FeedService feedService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return feedService.createPost(request.authorId(), request.content(), request.idempotencyKey());
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable String postId) {
        return feedService.getPost(postId);
    }
}

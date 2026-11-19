package com.project.feedme.controllers;

import com.project.feedme.dtos.TimelinePageResponse;
import com.project.feedme.services.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/home")
    public TimelinePageResponse getHomeFeed(@RequestParam String userId, @RequestParam(required = false) String cursor,
                                            @RequestParam(defaultValue = "5") int limit) {
        return feedService.getHomeFeed(userId, cursor, limit);
    }

    @GetMapping("/user/{userId}")
    public TimelinePageResponse getUserFeed(
            @PathVariable String userId, @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return feedService.getUserFeed(userId, cursor, limit);
    }
}

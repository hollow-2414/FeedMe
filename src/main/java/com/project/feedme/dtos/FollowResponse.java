package com.project.feedme.dtos;

public record FollowResponse(
        String followerId,
        String targetUserId,
        boolean following,
        int totalFollowing
) {
}

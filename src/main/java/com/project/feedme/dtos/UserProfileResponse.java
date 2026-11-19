package com.project.feedme.dtos;

public record UserProfileResponse(String id,
                                  String handle,
                                  String name,
                                  String bio,
                                  boolean hotUser) {
}

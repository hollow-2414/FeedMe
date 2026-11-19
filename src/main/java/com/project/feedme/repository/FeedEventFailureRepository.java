package com.project.feedme.repository;

import com.project.feedme.models.FeedEventFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedEventFailureRepository extends JpaRepository<FeedEventFailure, String> {
}

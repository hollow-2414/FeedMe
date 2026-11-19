package com.project.feedme.repository;

import com.project.feedme.models.ProcessedFeedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedFeedEventRepository extends JpaRepository<ProcessedFeedEvent, String> {
}

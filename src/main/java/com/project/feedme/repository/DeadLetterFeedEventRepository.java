package com.project.feedme.repository;

import com.project.feedme.models.DeadLetterFeedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterFeedEventRepository extends JpaRepository<DeadLetterFeedEvent, String> {
}

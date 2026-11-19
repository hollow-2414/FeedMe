package com.project.feedme.repository;

import com.project.feedme.models.OutboxEvent;
import com.project.feedme.models.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    // nextAttemptAt <= now
    List<OutboxEvent> findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxEventStatus status,
            Instant nextAttemptAt,
            Pageable pageable
    );
}

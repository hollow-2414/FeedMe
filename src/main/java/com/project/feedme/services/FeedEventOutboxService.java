package com.project.feedme.services;

import com.project.feedme.models.OutboxEvent;
import com.project.feedme.models.Post;
import com.project.feedme.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedEventOutboxService {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void enqueuePostCreated(Post post) {
        outboxEventRepository.save(new OutboxEvent(post));
    }
}

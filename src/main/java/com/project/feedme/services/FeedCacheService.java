package com.project.feedme.services;

import tools.jackson.core.JacksonException;
import com.project.feedme.dtos.FeedItemResponse;
import com.project.feedme.dtos.TimelinePageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedCacheService {
    public static final int DEFAULT_PAGE_SIZE = 5;

    private static final Duration HOME_FEED_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    private final FeedMetricsService feedMetricsService;

    private final ObjectMapper objectMapper;


    @CircuitBreaker(name = "feedCache", fallbackMethod = "getHomeFeedFallback")
    public Optional<TimelinePageResponse> getHomeFeed(String userId) {
        String payload = redisTemplate.opsForValue().get(homeFeedKey(userId));

        if (payload == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, TimelinePageResponse.class));
        } catch (Exception e) {
            log.info("Something wrong with redis while fetch for userId {}, {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
    private Optional<TimelinePageResponse> getHomeFeedFallback(String userId, Throwable t) {
        log.warn("feedCache circuit breaker — getHomeFeed degraded for userId={}: {}", userId, t.getMessage());
        return Optional.empty();
    }

    private String homeFeedKey(String userId) {
        return "feed:home:" + userId;
    }

    @CircuitBreaker(name = "feedCache", fallbackMethod = "cacheHomeFeedFallback")
    public void cacheHomeFeed(TimelinePageResponse timelinePageResponse) {
        if (writeHomeFeed(timelinePageResponse)) {
            feedMetricsService.recordCacheMutation("write_first_page");
        }
    }

    private void cacheHomeFeedFallback(TimelinePageResponse timelinePageResponse, Throwable t) {
        log.warn("feedCache circuit breaker — cacheHomeFeed degraded for userId={}: {}", timelinePageResponse.timelineOwnerId(), t.getMessage());
    }


    private boolean writeHomeFeed(TimelinePageResponse timelinePageResponse) {
        String payload;

        // 1st step Serialize the Java object into JSON.
        try {
            payload = objectMapper.writeValueAsString(timelinePageResponse);
        } catch (JacksonException e) {
            log.error(
                    "Failed to serialize home feed for userId {}: {}",
                    timelinePageResponse.timelineOwnerId(),
                    e.getMessage()
            );
            return false;
        }

        // 2nd step  Redis OUTSIDE the try-catch.
        // Redis exceptions can now propagate to the circuit breaker.
        redisTemplate.opsForValue().set(
                homeFeedKey(timelinePageResponse.timelineOwnerId()),
                payload,
                HOME_FEED_TTL
        );

        return true;
    }


    @CircuitBreaker(name = "feedCache", fallbackMethod = "evictHomeFeedFallback")
    public void evictHomeFeed(String userId) {
        redisTemplate.delete(homeFeedKey(userId));
        feedMetricsService.recordCacheMutation("evict");
    }

    private void evictHomeFeedFallback(String userId, Throwable t) {
        log.warn("feedCache circuit breaker — evictHomeFeed degraded for userId={}: {}", userId, t.getMessage());
    }

    @CircuitBreaker(name = "feedCache", fallbackMethod = "prependToHomeFeedFallback")
    public void prependToHomeFeed(String userId, FeedItemResponse item) {
        Optional<TimelinePageResponse> cachedFeed = getHomeFeed(userId);
        if (cachedFeed.isEmpty()) {
            return;
        }

        TimelinePageResponse existing = cachedFeed.get();

        List<FeedItemResponse> updatedItems = new ArrayList<>();
        updatedItems.add(item);

        existing.items().stream()
                .filter(existingItem -> !existingItem.postId().equals(item.postId()))
                .forEach(updatedItems::add);

        if (updatedItems.size() > DEFAULT_PAGE_SIZE) {
            updatedItems = updatedItems.subList(0, DEFAULT_PAGE_SIZE);
        }

        if (writeHomeFeed(new TimelinePageResponse(
                existing.timelineOwnerId(),
                existing.mode(),
                existing.totalItems() + 1,
                updatedItems,
                existing.totalItems() + 1 > updatedItems.size()
                        && !updatedItems.isEmpty()
                        ? FeedCursorCodec.encode(
                        updatedItems.get(updatedItems.size() - 1))
                        : null
        ))) {
            feedMetricsService.recordCacheMutation("prepend");
        }

    }

    private void prependToHomeFeedFallback(String userId, FeedItemResponse item, Throwable t) {
        log.warn("feedCache circuit breaker — prependToHomeFeed degraded for userId={}: {}", userId, t.getMessage());
    }

}

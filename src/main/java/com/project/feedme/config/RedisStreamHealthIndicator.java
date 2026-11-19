package com.project.feedme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component("redisStreamLag")
@RequiredArgsConstructor
public class RedisStreamHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate stringRedisTemplate;

    private static final long LAG_WARN_THRESHOLD = 1000L;
    private static final long LAG_DOWN_THRESHOLD = 10000L;

    @Override
    public Health health() {
        try {
            PendingMessagesSummary summary = stringRedisTemplate.opsForStream()
                    .pending("feed:events", "feed-workers");
            long pendingCount = summary != null ? summary.getTotalPendingMessages() : 0L;

            if (pendingCount > LAG_DOWN_THRESHOLD) {
                return Health.down()
                        .withDetail("pendingMessages", pendingCount)
                        .withDetail("threshold", LAG_DOWN_THRESHOLD)
                        .withDetail("reason", "Stream lag critical")
                        .build();
            } else if (pendingCount > LAG_WARN_THRESHOLD) {
                return Health.status("WARN")
                        .withDetail("pendingMessages", pendingCount)
                        .withDetail("threshold", LAG_WARN_THRESHOLD)
                        .withDetail("reason", "Stream lag elevated")
                        .build();
            }
            return Health.up()
                    .withDetail("pendingMessages", pendingCount)
                    .build();

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}

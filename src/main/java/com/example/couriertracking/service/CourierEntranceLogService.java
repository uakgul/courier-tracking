package com.example.couriertracking.service;

import com.example.couriertracking.domain.CourierEntranceLog;
import com.example.couriertracking.event.CourierLocationRecordedEvent;
import com.example.couriertracking.mapper.CourierEntranceLogMapper;
import com.example.couriertracking.model.dto.StoreDTO;
import com.example.couriertracking.repository.CourierEntranceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourierEntranceLogService {

    private static final long REENTRY_WINDOW_SECONDS = 60L;
    private static final String KEY_PREFIX = "courier-tracking:entrance:";

    private final CourierEntranceLogRepository courierEntranceLogRepository;
    private final StoreService storeService;
    private final CourierEntranceLogMapper courierEntranceLogMapper;
    private final StringRedisTemplate redisTemplate;

    public void detectEntrances(CourierLocationRecordedEvent event) {
        List<StoreDTO> storeList = storeService.getNearbyStores(event.latitude(), event.longitude());

        if (storeList.isEmpty()) {
            log.info("courierId {} is not near any store", event.courierId());
            return;
        }

        storeList.forEach(store -> registerEntrance(store, event));
    }

    private void registerEntrance(StoreDTO store, CourierLocationRecordedEvent event) {
        if (!isNewEntrance(event.courierId(), store.name(), event.eventTimeSeconds())) {
            log.info("courierId {} re-entered '{}' within the re-entry window - not counted as an entrance",
                    event.courierId(), store.name());
            return;
        }

        CourierEntranceLog entranceLog = courierEntranceLogMapper.toEntity(store, event);
        courierEntranceLogRepository.save(entranceLog);
        cacheLastEntrance(event.courierId(), store.name(), event.eventTimeSeconds());

        log.info("Store entrance | courierId={} store='{}' eventTimeSeconds={}",
                event.courierId(), store.name(), event.eventTimeSeconds());
    }

    private boolean isNewEntrance(Long courierId, String storeName, long eventTimeSeconds) {
        Long lastEntranceSeconds = readLastEntrance(courierId, storeName);

        return lastEntranceSeconds == null || eventTimeSeconds - lastEntranceSeconds >= REENTRY_WINDOW_SECONDS;
    }

    private Long readLastEntrance(Long courierId, String storeName) {
        try {
            String cached = redisTemplate.opsForValue().get(key(courierId, storeName));
            if (cached != null) {
                return Long.valueOf(cached);
            }
        } catch (DataAccessException e) {
            log.warn("Redis unavailable, falling back to the database for courierId={} store='{}'",
                    courierId, storeName, e);
        }

        return courierEntranceLogRepository
                .findTopByCourierIdAndStoreNameOrderByEventTimeSecondsDesc(courierId, storeName)
                .map(CourierEntranceLog::getEventTimeSeconds)
                .orElse(null);
    }

    private void cacheLastEntrance(Long courierId, String storeName, long eventTimeSeconds) {
        try {
            redisTemplate.opsForValue()
                    .set(this.key(courierId, storeName), Long.toString(eventTimeSeconds), Duration.ofSeconds(REENTRY_WINDOW_SECONDS));
        } catch (DataAccessException e) {
            log.warn("Could not cache the entrance for courierId={} store='{}'", courierId, storeName, e);
        }
    }

    private String key(Long courierId, String storeName) {
        return KEY_PREFIX + courierId + ":" + storeName;
    }
}

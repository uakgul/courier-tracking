package com.example.couriertracking.service;

import com.example.couriertracking.domain.CourierEntranceLog;
import com.example.couriertracking.event.CourierLocationRecordedEvent;
import com.example.couriertracking.mapper.CourierEntranceLogMapper;
import com.example.couriertracking.model.dto.StoreDTO;
import com.example.couriertracking.repository.CourierEntranceLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourierEntranceLogServiceTest {

    private static final StoreDTO STORE = new StoreDTO("Ataşehir MMM Migros", 40.9923307, 29.1244229);
    private static final long COURIER_ID = 1L;
    private static final String KEY = "courier-tracking:entrance:1:Ataşehir MMM Migros";
    private static final long T0 = 1_700_000_000L;

    @Mock
    private CourierEntranceLogRepository repository;
    @Mock
    private StoreService storeService;
    @Mock
    private CourierEntranceLogMapper mapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private CourierEntranceLogService service;

    @BeforeEach
    void setUp() {
        service = new CourierEntranceLogService(repository, storeService, mapper, redisTemplate);
    }

    @Test
    void positionAwayFromEveryStoreIsIgnored() {
        given(storeService.getNearbyStores(anyDouble(), anyDouble())).willReturn(List.of());

        service.detectEntrances(eventAt(T0));

        verify(repository, never()).save(any());
    }

    @Test
    void firstEntranceIsRecorded() {
        nearStore();
        cacheReturns(null);
        noPreviousEntrance();
        given(mapper.toEntity(any(), any())).willReturn(new CourierEntranceLog());

        service.detectEntrances(eventAt(T0));

        verify(repository).save(any());
    }

    @Test
    void reentryWithinOneMinuteIsIgnored() {
        nearStore();
        cacheReturns(String.valueOf(T0));

        service.detectEntrances(eventAt(T0 + 30));

        verify(repository, never()).save(any());
    }

    @Test
    void reentryAfterOneMinuteIsRecorded() {
        nearStore();
        cacheReturns(String.valueOf(T0));
        given(mapper.toEntity(any(), any())).willReturn(new CourierEntranceLog());

        service.detectEntrances(eventAt(T0 + 90));

        verify(repository).save(any());
        verify(valueOperations).set(KEY, String.valueOf(T0 + 90), Duration.ofSeconds(60));
    }

    @Test
    void entranceIsRecordedWhileRedisIsDown() {
        nearStore();
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(KEY)).willThrow(new RedisConnectionFailureException("connection refused"));
        noPreviousEntrance();
        given(mapper.toEntity(any(), any())).willReturn(new CourierEntranceLog());

        service.detectEntrances(eventAt(T0));

        verify(repository).save(any());
    }

    private void nearStore() {
        given(storeService.getNearbyStores(anyDouble(), anyDouble())).willReturn(List.of(STORE));
    }

    private void cacheReturns(String cached) {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(KEY)).willReturn(cached);
    }

    private void noPreviousEntrance() {
        given(repository.findTopByCourierIdAndStoreNameOrderByEventTimeSecondsDesc(COURIER_ID, STORE.name()))
                .willReturn(Optional.empty());
    }

    private CourierLocationRecordedEvent eventAt(long eventTimeSeconds) {
        return new CourierLocationRecordedEvent(COURIER_ID, STORE.latitude(), STORE.longitude(), eventTimeSeconds);
    }
}

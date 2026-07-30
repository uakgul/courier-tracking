package com.example.couriertracking.listener;

import com.example.couriertracking.config.AsyncConfig;
import com.example.couriertracking.event.CourierLocationRecordedEvent;
import com.example.couriertracking.service.CourierEntranceLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourierEntranceLogListener {

    private final CourierEntranceLogService courierEntranceLogService;

    @Async(AsyncConfig.ENTRANCE_DETECTION_EXECUTOR)
    @EventListener
    public void onCourierLocationRecorded(CourierLocationRecordedEvent event) {
        log.info("Handling location event for courierId {}", event.courierId());

        try {
            courierEntranceLogService.detectEntrances(event);
        } catch (Exception e) {
            log.error("Entrance detection failed for courierId {} at ({}, {}) eventTimeSeconds={}",
                    event.courierId(), event.latitude(), event.longitude(), event.eventTimeSeconds(), e);
        }
    }
}

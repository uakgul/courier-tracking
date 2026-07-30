package com.example.couriertracking.service;

import com.example.couriertracking.domain.CourierLocationLog;
import com.example.couriertracking.event.CourierLocationRecordedEvent;
import com.example.couriertracking.mapper.CourierLogMapper;
import com.example.couriertracking.model.dto.CourierLocationLogDTO;
import com.example.couriertracking.model.request.CourierLocationRequest;
import com.example.couriertracking.repository.CourierLocationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourierLocationLogService {

    private final CourierLocationLogRepository courierLocationLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CourierLogMapper courierLogMapper;

    public CourierLocationLogDTO saveCourierLocationEvent(CourierLocationRequest request) {
        log.info("Location received: {}", request);

        CourierLocationLog locationLog = new CourierLocationLog();
        locationLog.setCourierId(request.courierId());
        locationLog.setLatitude(request.latitude());
        locationLog.setLongitude(request.longitude());
        locationLog.setEventTimeSeconds(request.timeSeconds());

        CourierLocationLog savedLocationLog = courierLocationLogRepository.save(locationLog);

        eventPublisher.publishEvent(new CourierLocationRecordedEvent(
                request.courierId(), request.latitude(), request.longitude(), request.timeSeconds()));
        return courierLogMapper.toDTO(savedLocationLog);
    }
}

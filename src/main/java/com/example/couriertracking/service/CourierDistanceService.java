package com.example.couriertracking.service;

import com.example.couriertracking.domain.CourierLocationLog;
import com.example.couriertracking.repository.CourierLocationLogRepository;
import com.example.couriertracking.service.distance.DistanceCalculatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Gatherers;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourierDistanceService {

    private final CourierLocationLogRepository courierLocationLogRepository;
    private final DistanceCalculatorRegistry distanceCalculatorRegistry;

    @Transactional(readOnly = true)
    public Double getTotalTravelDistance(Long courierId) {
        List<CourierLocationLog> logs = courierLocationLogRepository.findByCourierIdOrderByEventTimeSecondsAsc(courierId);

        if (logs == null || logs.size() < 2) {
            log.info("courierId {} has fewer than two positions - total distance is 0", courierId);
            return 0.0d;
        }

        double totalDistanceMeters = logs.stream()
                .gather(Gatherers.windowSliding(2))
                .mapToDouble(pair -> distanceCalculatorRegistry.calculateDistance(
                        pair.getFirst().getLatitude(), pair.getFirst().getLongitude(),
                        pair.getLast().getLatitude(), pair.getLast().getLongitude()))
                .sum();

        log.info("courierId {} travelled {} m across {} positions", courierId, totalDistanceMeters, logs.size());
        return totalDistanceMeters;
    }
}

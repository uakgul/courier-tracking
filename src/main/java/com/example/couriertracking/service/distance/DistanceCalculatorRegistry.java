package com.example.couriertracking.service.distance;

import com.example.couriertracking.config.CourierProperties;
import com.example.couriertracking.model.enums.DistanceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DistanceCalculatorRegistry {

    private final Map<DistanceStrategy, DistanceCalculator> calculators;
    private final DistanceStrategy defaultStrategy;

    public DistanceCalculatorRegistry(List<DistanceCalculator> calculators, CourierProperties properties) {
        this.calculators = new EnumMap<>(DistanceStrategy.class);
        for (DistanceCalculator calculator : calculators) {
            DistanceCalculator previous = this.calculators.put(calculator.strategy(), calculator);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate DistanceCalculator registered for strategy " + calculator.strategy());
            }
        }

        this.defaultStrategy = properties.distance().strategy();
        if (!this.calculators.containsKey(this.defaultStrategy)) {
            throw new IllegalStateException("No DistanceCalculator implementation for configured "
                    + "default strategy " + this.defaultStrategy);
        }

        log.info("Distance strategies available: {} (default: {})", this.calculators.keySet(), this.defaultStrategy);
    }

    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        return calculateDistance(defaultStrategy, startLat, startLon, endLat, endLon);
    }

    public double calculateDistance(DistanceStrategy strategy,
                                    double startLat, double startLon,
                                    double endLat, double endLon) {
        DistanceCalculator calculator = calculators.get(strategy);
        if (calculator == null) {
            throw new IllegalArgumentException("Unknown distance strategy: " + strategy);
        }
        return calculator.calculateDistance(startLat, startLon, endLat, endLon);
    }
}

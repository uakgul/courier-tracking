package com.example.couriertracking.service.distance;

import com.example.couriertracking.model.enums.DistanceStrategy;
import org.springframework.stereotype.Component;

/**
 * reference:
 * https://www.baeldung.com/java-find-distance-between-points#calculate-the-distance-using-the-haversine-formula
 */
@Component
public class HaversineStrategy implements DistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    @Override
    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLon = Math.toRadians(endLon - startLon);

        double startLatRad = Math.toRadians(startLat);
        double endLatRad = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLatRad) * Math.cos(endLatRad) * haversine(dLon);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    @Override
    public DistanceStrategy strategy() {
        return DistanceStrategy.HAVERSINE;
    }

    private double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}

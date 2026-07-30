package com.example.couriertracking.service.distance;

import com.example.couriertracking.model.enums.DistanceStrategy;
import org.springframework.stereotype.Component;

/**
 * reference:
 * https://www.baeldung.com/java-find-distance-between-points#equirectangular-distance-approximation
 */
@Component
public class EquirectangularStrategy implements DistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    @Override
    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        double startLatRad = Math.toRadians(startLat);
        double endLatRad = Math.toRadians(endLat);
        double startLonRad = Math.toRadians(startLon);
        double endLonRad = Math.toRadians(endLon);

        double x = (endLonRad - startLonRad) * Math.cos((startLatRad + endLatRad) / 2);
        double y = endLatRad - startLatRad;

        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_METERS;
    }

    @Override
    public DistanceStrategy strategy() {
        return DistanceStrategy.EQUIRECTANGULAR;
    }
}

package com.example.couriertracking.service.distance;

import com.example.couriertracking.model.enums.DistanceStrategy;

public interface DistanceCalculator {

    double calculateDistance(double startLat, double startLon, double endLat, double endLon);

    DistanceStrategy strategy();
}

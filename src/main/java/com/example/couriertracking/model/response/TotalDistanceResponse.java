package com.example.couriertracking.model.response;

public record TotalDistanceResponse(
        Long courierId,
        Double totalDistanceMeters
) {
}

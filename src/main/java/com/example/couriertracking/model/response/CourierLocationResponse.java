package com.example.couriertracking.model.response;

public record CourierLocationResponse(
        Long timeSeconds,
        Long courierId,
        Double latitude,
        Double longitude
) {
}

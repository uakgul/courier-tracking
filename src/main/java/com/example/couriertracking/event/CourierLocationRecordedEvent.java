package com.example.couriertracking.event;

public record CourierLocationRecordedEvent(
        Long courierId,
        Double latitude,
        Double longitude,
        Long eventTimeSeconds
) {
}

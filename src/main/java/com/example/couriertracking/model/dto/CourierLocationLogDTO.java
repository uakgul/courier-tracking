package com.example.couriertracking.model.dto;

public record CourierLocationLogDTO(
        Long courierId,
        Double latitude,
        Double longitude,
        Long eventTimeSeconds
) {
}

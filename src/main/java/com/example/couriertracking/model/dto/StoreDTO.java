package com.example.couriertracking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreDTO(
        String name,

        @JsonProperty("lat")
        Double latitude,

        @JsonProperty("lng")
        Double longitude
) {
}

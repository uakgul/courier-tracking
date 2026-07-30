package com.example.couriertracking.config;

import com.example.couriertracking.model.enums.DistanceStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "courier")
public record CourierProperties(
        String storesLocation,
        Distance distance
) {

    public CourierProperties {
        if (storesLocation == null || storesLocation.isBlank()) {
            storesLocation = "classpath:stores.json";
        }
        if (distance == null) {
            distance = new Distance(null);
        }
    }


    public record Distance(DistanceStrategy strategy) {

        public Distance {
            if (strategy == null) {
                strategy = DistanceStrategy.HAVERSINE;
            }
        }
    }

}

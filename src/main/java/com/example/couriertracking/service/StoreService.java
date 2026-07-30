package com.example.couriertracking.service;

import com.example.couriertracking.config.CourierProperties;
import com.example.couriertracking.model.dto.StoreDTO;
import com.example.couriertracking.service.distance.DistanceCalculatorRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {

    private static final double ENTRANCE_RADIUS_METERS = 100.0;

    private final DistanceCalculatorRegistry distanceCalculatorRegistry;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final CourierProperties properties;

    private List<StoreDTO> stores = List.of();

    @PostConstruct
    void loadStores() {
        String location = properties.storesLocation();
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalStateException("Store catalogue not found at " + location);
        }

        List<StoreDTO> loaded;
        try (InputStream inputStream = resource.getInputStream()) {
            loaded = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Failed to read store catalogue from " + location, e);
        }

        if (loaded == null || loaded.isEmpty()) {
            throw new IllegalStateException("Store catalogue at " + location + " is empty");
        }
        this.stores = List.copyOf(loaded);

        log.info("Loaded {} stores from {}", stores.size(), location);
    }

    public List<StoreDTO> getNearbyStores(Double lat, Double lng) {
        List<StoreDTO> nearby = stores.stream()
                .filter(store -> distanceTo(lat, lng, store) <= ENTRANCE_RADIUS_METERS)
                .toList();

        log.info("Position ({}, {}) is within {} m of {} store(s)", lat, lng, ENTRANCE_RADIUS_METERS, nearby.size());
        return nearby;
    }

    private double distanceTo(Double lat, Double lng, StoreDTO store) {
        return distanceCalculatorRegistry.calculateDistance(lat, lng, store.latitude(), store.longitude());
    }
}

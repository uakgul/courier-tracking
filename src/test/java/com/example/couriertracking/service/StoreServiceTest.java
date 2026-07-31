package com.example.couriertracking.service;

import com.example.couriertracking.config.CourierProperties;
import com.example.couriertracking.model.dto.StoreDTO;
import com.example.couriertracking.service.distance.DistanceCalculatorRegistry;
import com.example.couriertracking.service.distance.HaversineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoreServiceTest {

    private static final double ATASEHIR_LAT = 40.9923307;
    private static final double ATASEHIR_LNG = 29.1244229;

    private StoreService storeService;

    @BeforeEach
    void setUp() {
        CourierProperties properties = new CourierProperties(null, null);
        DistanceCalculatorRegistry registry =
                new DistanceCalculatorRegistry(List.of(new HaversineStrategy()), properties);

        storeService = new StoreService(registry, new DefaultResourceLoader(), new ObjectMapper(), properties);
        storeService.loadStores();
    }

    @Test
    void readsCoordinatesFromTheCatalogue() {
        List<StoreDTO> nearby = storeService.getNearbyStores(ATASEHIR_LAT, ATASEHIR_LNG);

        assertThat(nearby)
                .singleElement()
                .satisfies(store -> {
                    assertThat(store.name()).isEqualTo("Ataşehir MMM Migros");
                    assertThat(store.latitude()).isEqualTo(ATASEHIR_LAT);
                    assertThat(store.longitude()).isEqualTo(ATASEHIR_LNG);
                });
    }

    @Test
    void matchesNoStoreWhenFarAway() {
        assertThat(storeService.getNearbyStores(41.5, 29.5)).isEmpty();
    }
}

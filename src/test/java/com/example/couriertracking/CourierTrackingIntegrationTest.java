package com.example.couriertracking;

import com.example.couriertracking.domain.CourierEntranceLog;
import com.example.couriertracking.repository.CourierEntranceLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourierTrackingIntegrationTest {

    private static final String LOCATIONS = "/api/v1/couriers/locations";
    private static final double ATASEHIR_LAT = 40.9923307;
    private static final double ATASEHIR_LNG = 29.1244229;
    private static final long T0 = 1_700_000_000L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CourierEntranceLogRepository entranceLogRepository;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void redisAlwaysMisses() {
        given(redisTemplate.opsForValue()).willReturn(mock(ValueOperations.class));
    }

    @Test
    void reentryWithinOneMinuteIsNotLoggedAsEntrance() throws Exception {
        sendLocation(101L, ATASEHIR_LAT, ATASEHIR_LNG, T0);
        sendLocation(101L, ATASEHIR_LAT, ATASEHIR_LNG, T0 + 30);
        sendLocation(101L, ATASEHIR_LAT, ATASEHIR_LNG, T0 + 90);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(entrancesOf(101L))
                        .extracting(CourierEntranceLog::getStoreName, CourierEntranceLog::getEventTimeSeconds)
                        .containsExactlyInAnyOrder(
                                tuple("Ataşehir MMM Migros", T0),
                                tuple("Ataşehir MMM Migros", T0 + 90)));
    }

    @Test
    void totalDistanceIsSummedAcrossPositions() throws Exception {
        sendLocation(102L, 41.10, 29.0, T0);
        sendLocation(102L, 41.11, 29.0, T0 + 60);

        mockMvc.perform(get("/api/v1/couriers/102/total-distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistanceMeters").value(closeTo(1112.0, 1.0)));
    }

    @Test
    void invalidLatitudeIsRejected() throws Exception {
        mockMvc.perform(post(LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timeSeconds":1700000000,"courierId":103,"latitude":200,"longitude":29.0}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.latitude").value("latitude must be between -90 and 90"));

        assertThat(entrancesOf(103L)).isEmpty();
    }

    private void sendLocation(long courierId, double latitude, double longitude, long timeSeconds) throws Exception {
        mockMvc.perform(post(LOCATIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timeSeconds":%d,"courierId":%d,"latitude":%s,"longitude":%s}"""
                                .formatted(timeSeconds, courierId, latitude, longitude)))
                .andExpect(status().isOk());
    }

    private List<CourierEntranceLog> entrancesOf(long courierId) {
        return StreamSupport.stream(entranceLogRepository.findAll().spliterator(), false)
                .filter(entrance -> entrance.getCourierId() == courierId)
                .toList();
    }
}

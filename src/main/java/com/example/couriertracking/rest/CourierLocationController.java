package com.example.couriertracking.rest;

import com.example.couriertracking.mapper.CourierLogMapper;
import com.example.couriertracking.model.dto.CourierLocationLogDTO;
import com.example.couriertracking.model.request.CourierLocationRequest;
import com.example.couriertracking.model.response.CourierLocationResponse;
import com.example.couriertracking.model.response.TotalDistanceResponse;
import com.example.couriertracking.service.CourierDistanceService;
import com.example.couriertracking.service.CourierLocationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
public class CourierLocationController {

    private final CourierLocationLogService courierLocationLogService;
    private final CourierDistanceService courierDistanceService;
    private final CourierLogMapper courierLogMapper;

    @GetMapping("/{courierId}/total-distance")
    public ResponseEntity<TotalDistanceResponse> getTotalTravelDistance(@PathVariable Long courierId) {
        var response = new TotalDistanceResponse(courierId, courierDistanceService.getTotalTravelDistance(courierId));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/locations")
    public ResponseEntity<CourierLocationResponse> logCourierLocation(@Valid @RequestBody CourierLocationRequest request) {
        CourierLocationLogDTO courierLocationLogDTO = courierLocationLogService.saveCourierLocationEvent(request);
        return ResponseEntity.ok().body(courierLogMapper.toResponse(courierLocationLogDTO));
    }
}

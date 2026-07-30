package com.example.couriertracking.repository;

import com.example.couriertracking.domain.CourierLocationLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierLocationLogRepository extends CrudRepository<CourierLocationLog, Long> {

    List<CourierLocationLog> findByCourierIdOrderByEventTimeSecondsAsc(Long courierId);
}

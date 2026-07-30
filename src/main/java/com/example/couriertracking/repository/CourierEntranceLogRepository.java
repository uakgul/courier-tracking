package com.example.couriertracking.repository;

import com.example.couriertracking.domain.CourierEntranceLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourierEntranceLogRepository extends CrudRepository<CourierEntranceLog, Long> {

    Optional<CourierEntranceLog> findTopByCourierIdAndStoreNameOrderByEventTimeSecondsDesc(Long courierId, String storeName);
}

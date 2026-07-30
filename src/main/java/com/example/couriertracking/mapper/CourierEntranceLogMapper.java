package com.example.couriertracking.mapper;

import com.example.couriertracking.domain.CourierEntranceLog;
import com.example.couriertracking.event.CourierLocationRecordedEvent;
import com.example.couriertracking.model.dto.StoreDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourierEntranceLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "courierId", source = "event.courierId")
    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "eventTimeSeconds", source = "event.eventTimeSeconds")
    CourierEntranceLog toEntity(StoreDTO store, CourierLocationRecordedEvent event);
}

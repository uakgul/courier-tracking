package com.example.couriertracking.mapper;

import com.example.couriertracking.domain.CourierLocationLog;
import com.example.couriertracking.model.dto.CourierLocationLogDTO;
import com.example.couriertracking.model.response.CourierLocationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourierLogMapper {

    CourierLocationLogDTO toDTO(CourierLocationLog courierLocationLog);

    @Mapping(target = "timeSeconds", source = "eventTimeSeconds")
    CourierLocationResponse toResponse(CourierLocationLogDTO courierLocationLogDTO);
}

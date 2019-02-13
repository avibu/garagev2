package com.braude.garage.service.mapper;

import com.braude.garage.domain.*;
import com.braude.garage.service.dto.CarDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Car and its DTO CarDTO.
 */
@Mapper(componentModel = "spring", uses = {ClientMapper.class})
public interface CarMapper extends EntityMapper<CarDTO, Car> {

    @Mapping(source = "client.id", target = "clientId")
    CarDTO toDto(Car car);

    @Mapping(source = "clientId", target = "client")
    @Mapping(target = "carServices", ignore = true)
    Car toEntity(CarDTO carDTO);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        Car car = new Car();
        car.setId(id);
        return car;
    }
}

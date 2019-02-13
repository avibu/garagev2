package com.braude.garage.service.mapper;

import com.braude.garage.domain.*;
import com.braude.garage.service.dto.CarServiceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CarService and its DTO CarServiceDTO.
 */
@Mapper(componentModel = "spring", uses = {CarMapper.class})
public interface CarServiceMapper extends EntityMapper<CarServiceDTO, CarService> {

    @Mapping(source = "car.id", target = "carId")
    CarServiceDTO toDto(CarService carService);

    @Mapping(source = "carId", target = "car")
    CarService toEntity(CarServiceDTO carServiceDTO);

    default CarService fromId(Long id) {
        if (id == null) {
            return null;
        }
        CarService carService = new CarService();
        carService.setId(id);
        return carService;
    }
}

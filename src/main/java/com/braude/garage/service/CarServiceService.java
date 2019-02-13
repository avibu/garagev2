package com.braude.garage.service;

import com.braude.garage.service.dto.CarServiceDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing CarService.
 */
public interface CarServiceService {

    /**
     * Save a carService.
     *
     * @param carServiceDTO the entity to save
     * @return the persisted entity
     */
    CarServiceDTO save(CarServiceDTO carServiceDTO);

    /**
     * Get all the carServices.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<CarServiceDTO> findAll(Pageable pageable);


    /**
     * Get the "id" carService.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<CarServiceDTO> findOne(Long id);

    /**
     * Delete the "id" carService.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the carService corresponding to the query.
     *
     * @param query the query of the search
     * 
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<CarServiceDTO> search(String query, Pageable pageable);
}

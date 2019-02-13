package com.braude.garage.service.impl;

import com.braude.garage.service.CarServiceService;
import com.braude.garage.domain.CarService;
import com.braude.garage.repository.CarServiceRepository;
import com.braude.garage.repository.search.CarServiceSearchRepository;
import com.braude.garage.service.dto.CarServiceDTO;
import com.braude.garage.service.mapper.CarServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing CarService.
 */
@Service
@Transactional
public class CarServiceServiceImpl implements CarServiceService {

    private final Logger log = LoggerFactory.getLogger(CarServiceServiceImpl.class);

    private final CarServiceRepository carServiceRepository;

    private final CarServiceMapper carServiceMapper;

    private final CarServiceSearchRepository carServiceSearchRepository;

    public CarServiceServiceImpl(CarServiceRepository carServiceRepository, CarServiceMapper carServiceMapper, CarServiceSearchRepository carServiceSearchRepository) {
        this.carServiceRepository = carServiceRepository;
        this.carServiceMapper = carServiceMapper;
        this.carServiceSearchRepository = carServiceSearchRepository;
    }

    /**
     * Save a carService.
     *
     * @param carServiceDTO the entity to save
     * @return the persisted entity
     */
    @Override
    public CarServiceDTO save(CarServiceDTO carServiceDTO) {
        log.debug("Request to save CarService : {}", carServiceDTO);
        CarService carService = carServiceMapper.toEntity(carServiceDTO);
        carService = carServiceRepository.save(carService);
        CarServiceDTO result = carServiceMapper.toDto(carService);
        carServiceSearchRepository.save(carService);
        return result;
    }

    /**
     * Get all the carServices.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarServiceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CarServices");
        return carServiceRepository.findAll(pageable)
            .map(carServiceMapper::toDto);
    }


    /**
     * Get one carService by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CarServiceDTO> findOne(Long id) {
        log.debug("Request to get CarService : {}", id);
        return carServiceRepository.findById(id)
            .map(carServiceMapper::toDto);
    }

    /**
     * Delete the carService by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete CarService : {}", id);        carServiceRepository.deleteById(id);
        carServiceSearchRepository.deleteById(id);
    }

    /**
     * Search for the carService corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CarServiceDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of CarServices for query {}", query);
        return carServiceSearchRepository.search(queryStringQuery(query), pageable)
            .map(carServiceMapper::toDto);
    }
}

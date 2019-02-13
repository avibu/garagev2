package com.braude.garage.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.braude.garage.domain.Car;
import com.braude.garage.domain.*; // for static metamodels
import com.braude.garage.repository.CarRepository;
import com.braude.garage.repository.search.CarSearchRepository;
import com.braude.garage.service.dto.CarCriteria;
import com.braude.garage.service.dto.CarDTO;
import com.braude.garage.service.mapper.CarMapper;

/**
 * Service for executing complex queries for Car entities in the database.
 * The main input is a {@link CarCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CarDTO} or a {@link Page} of {@link CarDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CarQueryService extends QueryService<Car> {

    private final Logger log = LoggerFactory.getLogger(CarQueryService.class);

    private final CarRepository carRepository;

    private final CarMapper carMapper;

    private final CarSearchRepository carSearchRepository;

    public CarQueryService(CarRepository carRepository, CarMapper carMapper, CarSearchRepository carSearchRepository) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
        this.carSearchRepository = carSearchRepository;
    }

    /**
     * Return a {@link List} of {@link CarDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CarDTO> findByCriteria(CarCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Car> specification = createSpecification(criteria);
        return carMapper.toDto(carRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CarDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CarDTO> findByCriteria(CarCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Car> specification = createSpecification(criteria);
        return carRepository.findAll(specification, page)
            .map(carMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CarCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Car> specification = createSpecification(criteria);
        return carRepository.count(specification);
    }

    /**
     * Function to convert CarCriteria to a {@link Specification}
     */
    private Specification<Car> createSpecification(CarCriteria criteria) {
        Specification<Car> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Car_.id));
            }
            if (criteria.getLicensePlate() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLicensePlate(), Car_.licensePlate));
            }
            if (criteria.getMake() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMake(), Car_.make));
            }
            if (criteria.getModel() != null) {
                specification = specification.and(buildStringSpecification(criteria.getModel(), Car_.model));
            }
            if (criteria.getYear() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getYear(), Car_.year));
            }
            if (criteria.getClientId() != null) {
                specification = specification.and(buildSpecification(criteria.getClientId(),
                    root -> root.join(Car_.client, JoinType.LEFT).get(Client_.id)));
            }
            if (criteria.getCarServiceId() != null) {
                specification = specification.and(buildSpecification(criteria.getCarServiceId(),
                    root -> root.join(Car_.carServices, JoinType.LEFT).get(CarService_.id)));
            }
        }
        return specification;
    }
}

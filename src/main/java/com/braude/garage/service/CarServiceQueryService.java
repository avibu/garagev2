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

import com.braude.garage.domain.CarService;
import com.braude.garage.domain.*; // for static metamodels
import com.braude.garage.repository.CarServiceRepository;
import com.braude.garage.repository.search.CarServiceSearchRepository;
import com.braude.garage.service.dto.CarServiceCriteria;
import com.braude.garage.service.dto.CarServiceDTO;
import com.braude.garage.service.mapper.CarServiceMapper;

/**
 * Service for executing complex queries for CarService entities in the database.
 * The main input is a {@link CarServiceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CarServiceDTO} or a {@link Page} of {@link CarServiceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CarServiceQueryService extends QueryService<CarService> {

    private final Logger log = LoggerFactory.getLogger(CarServiceQueryService.class);

    private final CarServiceRepository carServiceRepository;

    private final CarServiceMapper carServiceMapper;

    private final CarServiceSearchRepository carServiceSearchRepository;

    public CarServiceQueryService(CarServiceRepository carServiceRepository, CarServiceMapper carServiceMapper, CarServiceSearchRepository carServiceSearchRepository) {
        this.carServiceRepository = carServiceRepository;
        this.carServiceMapper = carServiceMapper;
        this.carServiceSearchRepository = carServiceSearchRepository;
    }

    /**
     * Return a {@link List} of {@link CarServiceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CarServiceDTO> findByCriteria(CarServiceCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CarService> specification = createSpecification(criteria);
        return carServiceMapper.toDto(carServiceRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CarServiceDTO} which matches the criteria from the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CarServiceDTO> findByCriteria(CarServiceCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CarService> specification = createSpecification(criteria);
        return carServiceRepository.findAll(specification, page)
            .map(carServiceMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CarServiceCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CarService> specification = createSpecification(criteria);
        return carServiceRepository.count(specification);
    }

    /**
     * Function to convert CarServiceCriteria to a {@link Specification}
     */
    private Specification<CarService> createSpecification(CarServiceCriteria criteria) {
        Specification<CarService> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), CarService_.id));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), CarService_.date));
            }
            if (criteria.getDescription() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescription(), CarService_.description));
            }
            if (criteria.getTotalCost() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalCost(), CarService_.totalCost));
            }
            if (criteria.getCarId() != null) {
                specification = specification.and(buildSpecification(criteria.getCarId(),
                    root -> root.join(CarService_.car, JoinType.LEFT).get(Car_.id)));
            }
        }
        return specification;
    }
}

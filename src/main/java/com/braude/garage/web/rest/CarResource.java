package com.braude.garage.web.rest;
import com.braude.garage.service.CarService;
import com.braude.garage.web.rest.errors.BadRequestAlertException;
import com.braude.garage.web.rest.util.HeaderUtil;
import com.braude.garage.web.rest.util.PaginationUtil;
import com.braude.garage.service.dto.CarDTO;
import com.braude.garage.service.dto.CarCriteria;
import com.braude.garage.service.CarQueryService;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Car.
 */
@RestController
@RequestMapping("/api")
public class CarResource {

    private final Logger log = LoggerFactory.getLogger(CarResource.class);

    private static final String ENTITY_NAME = "car";

    private final CarService carService;

    private final CarQueryService carQueryService;

    public CarResource(CarService carService, CarQueryService carQueryService) {
        this.carService = carService;
        this.carQueryService = carQueryService;
    }

    /**
     * POST  /cars : Create a new car.
     *
     * @param carDTO the carDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new carDTO, or with status 400 (Bad Request) if the car has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/cars")
    public ResponseEntity<CarDTO> createCar(@RequestBody CarDTO carDTO) throws URISyntaxException {
        log.debug("REST request to save Car : {}", carDTO);
        if (carDTO.getId() != null) {
            throw new BadRequestAlertException("A new car cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CarDTO result = carService.save(carDTO);
        return ResponseEntity.created(new URI("/api/cars/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /cars : Updates an existing car.
     *
     * @param carDTO the carDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated carDTO,
     * or with status 400 (Bad Request) if the carDTO is not valid,
     * or with status 500 (Internal Server Error) if the carDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/cars")
    public ResponseEntity<CarDTO> updateCar(@RequestBody CarDTO carDTO) throws URISyntaxException {
        log.debug("REST request to update Car : {}", carDTO);
        if (carDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CarDTO result = carService.save(carDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, carDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /cars : get all the cars.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of cars in body
     */
    @GetMapping("/cars")
    public ResponseEntity<List<CarDTO>> getAllCars(CarCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Cars by criteria: {}", criteria);
        Page<CarDTO> page = carQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/cars");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * GET  /cars/count : count all the cars.
    *
    * @param criteria the criterias which the requested entities should match
    * @return the ResponseEntity with status 200 (OK) and the count in body
    */
    @GetMapping("/cars/count")
    public ResponseEntity<Long> countCars(CarCriteria criteria) {
        log.debug("REST request to count Cars by criteria: {}", criteria);
        return ResponseEntity.ok().body(carQueryService.countByCriteria(criteria));
    }

    /**
     * GET  /cars/:id : get the "id" car.
     *
     * @param id the id of the carDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the carDTO, or with status 404 (Not Found)
     */
    @GetMapping("/cars/{id}")
    public ResponseEntity<CarDTO> getCar(@PathVariable Long id) {
        log.debug("REST request to get Car : {}", id);
        Optional<CarDTO> carDTO = carService.findOne(id);
        return ResponseUtil.wrapOrNotFound(carDTO);
    }

    /**
     * DELETE  /cars/:id : delete the "id" car.
     *
     * @param id the id of the carDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/cars/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        log.debug("REST request to delete Car : {}", id);
        carService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/cars?query=:query : search for the car corresponding
     * to the query.
     *
     * @param query the query of the car search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/cars")
    public ResponseEntity<List<CarDTO>> searchCars(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Cars for query {}", query);
        Page<CarDTO> page = carService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/cars");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}

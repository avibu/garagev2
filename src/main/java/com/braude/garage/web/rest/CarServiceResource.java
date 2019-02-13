package com.braude.garage.web.rest;
import com.braude.garage.service.CarServiceService;
import com.braude.garage.web.rest.errors.BadRequestAlertException;
import com.braude.garage.web.rest.util.HeaderUtil;
import com.braude.garage.web.rest.util.PaginationUtil;
import com.braude.garage.service.dto.CarServiceDTO;
import com.braude.garage.service.dto.CarServiceCriteria;
import com.braude.garage.service.CarServiceQueryService;
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
 * REST controller for managing CarService.
 */
@RestController
@RequestMapping("/api")
public class CarServiceResource {

    private final Logger log = LoggerFactory.getLogger(CarServiceResource.class);

    private static final String ENTITY_NAME = "carService";

    private final CarServiceService carServiceService;

    private final CarServiceQueryService carServiceQueryService;

    public CarServiceResource(CarServiceService carServiceService, CarServiceQueryService carServiceQueryService) {
        this.carServiceService = carServiceService;
        this.carServiceQueryService = carServiceQueryService;
    }

    /**
     * POST  /car-services : Create a new carService.
     *
     * @param carServiceDTO the carServiceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new carServiceDTO, or with status 400 (Bad Request) if the carService has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/car-services")
    public ResponseEntity<CarServiceDTO> createCarService(@RequestBody CarServiceDTO carServiceDTO) throws URISyntaxException {
        log.debug("REST request to save CarService : {}", carServiceDTO);
        if (carServiceDTO.getId() != null) {
            throw new BadRequestAlertException("A new carService cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CarServiceDTO result = carServiceService.save(carServiceDTO);
        return ResponseEntity.created(new URI("/api/car-services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /car-services : Updates an existing carService.
     *
     * @param carServiceDTO the carServiceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated carServiceDTO,
     * or with status 400 (Bad Request) if the carServiceDTO is not valid,
     * or with status 500 (Internal Server Error) if the carServiceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/car-services")
    public ResponseEntity<CarServiceDTO> updateCarService(@RequestBody CarServiceDTO carServiceDTO) throws URISyntaxException {
        log.debug("REST request to update CarService : {}", carServiceDTO);
        if (carServiceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CarServiceDTO result = carServiceService.save(carServiceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, carServiceDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /car-services : get all the carServices.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of carServices in body
     */
    @GetMapping("/car-services")
    public ResponseEntity<List<CarServiceDTO>> getAllCarServices(CarServiceCriteria criteria, Pageable pageable) {
        log.debug("REST request to get CarServices by criteria: {}", criteria);
        Page<CarServiceDTO> page = carServiceQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/car-services");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * GET  /car-services/count : count all the carServices.
    *
    * @param criteria the criterias which the requested entities should match
    * @return the ResponseEntity with status 200 (OK) and the count in body
    */
    @GetMapping("/car-services/count")
    public ResponseEntity<Long> countCarServices(CarServiceCriteria criteria) {
        log.debug("REST request to count CarServices by criteria: {}", criteria);
        return ResponseEntity.ok().body(carServiceQueryService.countByCriteria(criteria));
    }

    /**
     * GET  /car-services/:id : get the "id" carService.
     *
     * @param id the id of the carServiceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the carServiceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/car-services/{id}")
    public ResponseEntity<CarServiceDTO> getCarService(@PathVariable Long id) {
        log.debug("REST request to get CarService : {}", id);
        Optional<CarServiceDTO> carServiceDTO = carServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(carServiceDTO);
    }

    /**
     * DELETE  /car-services/:id : delete the "id" carService.
     *
     * @param id the id of the carServiceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/car-services/{id}")
    public ResponseEntity<Void> deleteCarService(@PathVariable Long id) {
        log.debug("REST request to delete CarService : {}", id);
        carServiceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/car-services?query=:query : search for the carService corresponding
     * to the query.
     *
     * @param query the query of the carService search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/car-services")
    public ResponseEntity<List<CarServiceDTO>> searchCarServices(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of CarServices for query {}", query);
        Page<CarServiceDTO> page = carServiceService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/car-services");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}

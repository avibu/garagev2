package com.braude.garage.web.rest;

import com.braude.garage.GarageApp;

import com.braude.garage.domain.CarService;
import com.braude.garage.domain.Car;
import com.braude.garage.repository.CarServiceRepository;
import com.braude.garage.repository.search.CarServiceSearchRepository;
import com.braude.garage.service.CarServiceService;
import com.braude.garage.service.dto.CarServiceDTO;
import com.braude.garage.service.mapper.CarServiceMapper;
import com.braude.garage.web.rest.errors.ExceptionTranslator;
import com.braude.garage.service.dto.CarServiceCriteria;
import com.braude.garage.service.CarServiceQueryService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;


import static com.braude.garage.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CarServiceResource REST controller.
 *
 * @see CarServiceResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GarageApp.class)
public class CarServiceResourceIntTest {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Double DEFAULT_TOTAL_COST = 1D;
    private static final Double UPDATED_TOTAL_COST = 2D;

    @Autowired
    private CarServiceRepository carServiceRepository;

    @Autowired
    private CarServiceMapper carServiceMapper;

    @Autowired
    private CarServiceService carServiceService;

    /**
     * This repository is mocked in the com.braude.garage.repository.search test package.
     *
     * @see com.braude.garage.repository.search.CarServiceSearchRepositoryMockConfiguration
     */
    @Autowired
    private CarServiceSearchRepository mockCarServiceSearchRepository;

    @Autowired
    private CarServiceQueryService carServiceQueryService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restCarServiceMockMvc;

    private CarService carService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CarServiceResource carServiceResource = new CarServiceResource(carServiceService, carServiceQueryService);
        this.restCarServiceMockMvc = MockMvcBuilders.standaloneSetup(carServiceResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CarService createEntity(EntityManager em) {
        CarService carService = new CarService()
            .date(DEFAULT_DATE)
            .description(DEFAULT_DESCRIPTION)
            .totalCost(DEFAULT_TOTAL_COST);
        return carService;
    }

    @Before
    public void initTest() {
        carService = createEntity(em);
    }

    @Test
    @Transactional
    public void createCarService() throws Exception {
        int databaseSizeBeforeCreate = carServiceRepository.findAll().size();

        // Create the CarService
        CarServiceDTO carServiceDTO = carServiceMapper.toDto(carService);
        restCarServiceMockMvc.perform(post("/api/car-services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carServiceDTO)))
            .andExpect(status().isCreated());

        // Validate the CarService in the database
        List<CarService> carServiceList = carServiceRepository.findAll();
        assertThat(carServiceList).hasSize(databaseSizeBeforeCreate + 1);
        CarService testCarService = carServiceList.get(carServiceList.size() - 1);
        assertThat(testCarService.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testCarService.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testCarService.getTotalCost()).isEqualTo(DEFAULT_TOTAL_COST);

        // Validate the CarService in Elasticsearch
        verify(mockCarServiceSearchRepository, times(1)).save(testCarService);
    }

    @Test
    @Transactional
    public void createCarServiceWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = carServiceRepository.findAll().size();

        // Create the CarService with an existing ID
        carService.setId(1L);
        CarServiceDTO carServiceDTO = carServiceMapper.toDto(carService);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarServiceMockMvc.perform(post("/api/car-services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carServiceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CarService in the database
        List<CarService> carServiceList = carServiceRepository.findAll();
        assertThat(carServiceList).hasSize(databaseSizeBeforeCreate);

        // Validate the CarService in Elasticsearch
        verify(mockCarServiceSearchRepository, times(0)).save(carService);
    }

    @Test
    @Transactional
    public void getAllCarServices() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList
        restCarServiceMockMvc.perform(get("/api/car-services?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carService.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].totalCost").value(hasItem(DEFAULT_TOTAL_COST.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getCarService() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get the carService
        restCarServiceMockMvc.perform(get("/api/car-services/{id}", carService.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(carService.getId().intValue()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.totalCost").value(DEFAULT_TOTAL_COST.doubleValue()));
    }

    @Test
    @Transactional
    public void getAllCarServicesByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where date equals to DEFAULT_DATE
        defaultCarServiceShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the carServiceList where date equals to UPDATED_DATE
        defaultCarServiceShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCarServicesByDateIsInShouldWork() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where date in DEFAULT_DATE or UPDATED_DATE
        defaultCarServiceShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the carServiceList where date equals to UPDATED_DATE
        defaultCarServiceShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCarServicesByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where date is not null
        defaultCarServiceShouldBeFound("date.specified=true");

        // Get all the carServiceList where date is null
        defaultCarServiceShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarServicesByDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where date greater than or equals to DEFAULT_DATE
        defaultCarServiceShouldBeFound("date.greaterOrEqualThan=" + DEFAULT_DATE);

        // Get all the carServiceList where date greater than or equals to UPDATED_DATE
        defaultCarServiceShouldNotBeFound("date.greaterOrEqualThan=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllCarServicesByDateIsLessThanSomething() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where date less than or equals to DEFAULT_DATE
        defaultCarServiceShouldNotBeFound("date.lessThan=" + DEFAULT_DATE);

        // Get all the carServiceList where date less than or equals to UPDATED_DATE
        defaultCarServiceShouldBeFound("date.lessThan=" + UPDATED_DATE);
    }


    @Test
    @Transactional
    public void getAllCarServicesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where description equals to DEFAULT_DESCRIPTION
        defaultCarServiceShouldBeFound("description.equals=" + DEFAULT_DESCRIPTION);

        // Get all the carServiceList where description equals to UPDATED_DESCRIPTION
        defaultCarServiceShouldNotBeFound("description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCarServicesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultCarServiceShouldBeFound("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION);

        // Get all the carServiceList where description equals to UPDATED_DESCRIPTION
        defaultCarServiceShouldNotBeFound("description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    public void getAllCarServicesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where description is not null
        defaultCarServiceShouldBeFound("description.specified=true");

        // Get all the carServiceList where description is null
        defaultCarServiceShouldNotBeFound("description.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarServicesByTotalCostIsEqualToSomething() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where totalCost equals to DEFAULT_TOTAL_COST
        defaultCarServiceShouldBeFound("totalCost.equals=" + DEFAULT_TOTAL_COST);

        // Get all the carServiceList where totalCost equals to UPDATED_TOTAL_COST
        defaultCarServiceShouldNotBeFound("totalCost.equals=" + UPDATED_TOTAL_COST);
    }

    @Test
    @Transactional
    public void getAllCarServicesByTotalCostIsInShouldWork() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where totalCost in DEFAULT_TOTAL_COST or UPDATED_TOTAL_COST
        defaultCarServiceShouldBeFound("totalCost.in=" + DEFAULT_TOTAL_COST + "," + UPDATED_TOTAL_COST);

        // Get all the carServiceList where totalCost equals to UPDATED_TOTAL_COST
        defaultCarServiceShouldNotBeFound("totalCost.in=" + UPDATED_TOTAL_COST);
    }

    @Test
    @Transactional
    public void getAllCarServicesByTotalCostIsNullOrNotNull() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        // Get all the carServiceList where totalCost is not null
        defaultCarServiceShouldBeFound("totalCost.specified=true");

        // Get all the carServiceList where totalCost is null
        defaultCarServiceShouldNotBeFound("totalCost.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarServicesByCarIsEqualToSomething() throws Exception {
        // Initialize the database
        Car car = CarResourceIntTest.createEntity(em);
        em.persist(car);
        em.flush();
        carService.setCar(car);
        carServiceRepository.saveAndFlush(carService);
        Long carId = car.getId();

        // Get all the carServiceList where car equals to carId
        defaultCarServiceShouldBeFound("carId.equals=" + carId);

        // Get all the carServiceList where car equals to carId + 1
        defaultCarServiceShouldNotBeFound("carId.equals=" + (carId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultCarServiceShouldBeFound(String filter) throws Exception {
        restCarServiceMockMvc.perform(get("/api/car-services?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carService.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].totalCost").value(hasItem(DEFAULT_TOTAL_COST.doubleValue())));

        // Check, that the count call also returns 1
        restCarServiceMockMvc.perform(get("/api/car-services/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultCarServiceShouldNotBeFound(String filter) throws Exception {
        restCarServiceMockMvc.perform(get("/api/car-services?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarServiceMockMvc.perform(get("/api/car-services/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingCarService() throws Exception {
        // Get the carService
        restCarServiceMockMvc.perform(get("/api/car-services/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCarService() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        int databaseSizeBeforeUpdate = carServiceRepository.findAll().size();

        // Update the carService
        CarService updatedCarService = carServiceRepository.findById(carService.getId()).get();
        // Disconnect from session so that the updates on updatedCarService are not directly saved in db
        em.detach(updatedCarService);
        updatedCarService
            .date(UPDATED_DATE)
            .description(UPDATED_DESCRIPTION)
            .totalCost(UPDATED_TOTAL_COST);
        CarServiceDTO carServiceDTO = carServiceMapper.toDto(updatedCarService);

        restCarServiceMockMvc.perform(put("/api/car-services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carServiceDTO)))
            .andExpect(status().isOk());

        // Validate the CarService in the database
        List<CarService> carServiceList = carServiceRepository.findAll();
        assertThat(carServiceList).hasSize(databaseSizeBeforeUpdate);
        CarService testCarService = carServiceList.get(carServiceList.size() - 1);
        assertThat(testCarService.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testCarService.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testCarService.getTotalCost()).isEqualTo(UPDATED_TOTAL_COST);

        // Validate the CarService in Elasticsearch
        verify(mockCarServiceSearchRepository, times(1)).save(testCarService);
    }

    @Test
    @Transactional
    public void updateNonExistingCarService() throws Exception {
        int databaseSizeBeforeUpdate = carServiceRepository.findAll().size();

        // Create the CarService
        CarServiceDTO carServiceDTO = carServiceMapper.toDto(carService);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarServiceMockMvc.perform(put("/api/car-services")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carServiceDTO)))
            .andExpect(status().isBadRequest());

        // Validate the CarService in the database
        List<CarService> carServiceList = carServiceRepository.findAll();
        assertThat(carServiceList).hasSize(databaseSizeBeforeUpdate);

        // Validate the CarService in Elasticsearch
        verify(mockCarServiceSearchRepository, times(0)).save(carService);
    }

    @Test
    @Transactional
    public void deleteCarService() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);

        int databaseSizeBeforeDelete = carServiceRepository.findAll().size();

        // Delete the carService
        restCarServiceMockMvc.perform(delete("/api/car-services/{id}", carService.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<CarService> carServiceList = carServiceRepository.findAll();
        assertThat(carServiceList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the CarService in Elasticsearch
        verify(mockCarServiceSearchRepository, times(1)).deleteById(carService.getId());
    }

    @Test
    @Transactional
    public void searchCarService() throws Exception {
        // Initialize the database
        carServiceRepository.saveAndFlush(carService);
        when(mockCarServiceSearchRepository.search(queryStringQuery("id:" + carService.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(carService), PageRequest.of(0, 1), 1));
        // Search the carService
        restCarServiceMockMvc.perform(get("/api/_search/car-services?query=id:" + carService.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(carService.getId().intValue())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].totalCost").value(hasItem(DEFAULT_TOTAL_COST.doubleValue())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarService.class);
        CarService carService1 = new CarService();
        carService1.setId(1L);
        CarService carService2 = new CarService();
        carService2.setId(carService1.getId());
        assertThat(carService1).isEqualTo(carService2);
        carService2.setId(2L);
        assertThat(carService1).isNotEqualTo(carService2);
        carService1.setId(null);
        assertThat(carService1).isNotEqualTo(carService2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarServiceDTO.class);
        CarServiceDTO carServiceDTO1 = new CarServiceDTO();
        carServiceDTO1.setId(1L);
        CarServiceDTO carServiceDTO2 = new CarServiceDTO();
        assertThat(carServiceDTO1).isNotEqualTo(carServiceDTO2);
        carServiceDTO2.setId(carServiceDTO1.getId());
        assertThat(carServiceDTO1).isEqualTo(carServiceDTO2);
        carServiceDTO2.setId(2L);
        assertThat(carServiceDTO1).isNotEqualTo(carServiceDTO2);
        carServiceDTO1.setId(null);
        assertThat(carServiceDTO1).isNotEqualTo(carServiceDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(carServiceMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(carServiceMapper.fromId(null)).isNull();
    }
}

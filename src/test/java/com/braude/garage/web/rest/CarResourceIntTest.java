package com.braude.garage.web.rest;

import com.braude.garage.GarageApp;

import com.braude.garage.domain.Car;
import com.braude.garage.domain.Client;
import com.braude.garage.domain.CarService;
import com.braude.garage.repository.CarRepository;
import com.braude.garage.repository.search.CarSearchRepository;
import com.braude.garage.service.CarService;
import com.braude.garage.service.dto.CarDTO;
import com.braude.garage.service.mapper.CarMapper;
import com.braude.garage.web.rest.errors.ExceptionTranslator;
import com.braude.garage.service.dto.CarCriteria;
import com.braude.garage.service.CarQueryService;

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
 * Test class for the CarResource REST controller.
 *
 * @see CarResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GarageApp.class)
public class CarResourceIntTest {

    private static final String DEFAULT_LICENSE_PLATE = "AAAAAAAAAA";
    private static final String UPDATED_LICENSE_PLATE = "BBBBBBBBBB";

    private static final String DEFAULT_MAKE = "AAAAAAAAAA";
    private static final String UPDATED_MAKE = "BBBBBBBBBB";

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final Integer DEFAULT_YEAR = 1;
    private static final Integer UPDATED_YEAR = 2;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private CarService carService;

    /**
     * This repository is mocked in the com.braude.garage.repository.search test package.
     *
     * @see com.braude.garage.repository.search.CarSearchRepositoryMockConfiguration
     */
    @Autowired
    private CarSearchRepository mockCarSearchRepository;

    @Autowired
    private CarQueryService carQueryService;

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

    private MockMvc restCarMockMvc;

    private Car car;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CarResource carResource = new CarResource(carService, carQueryService);
        this.restCarMockMvc = MockMvcBuilders.standaloneSetup(carResource)
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
    public static Car createEntity(EntityManager em) {
        Car car = new Car()
            .licensePlate(DEFAULT_LICENSE_PLATE)
            .make(DEFAULT_MAKE)
            .model(DEFAULT_MODEL)
            .year(DEFAULT_YEAR);
        return car;
    }

    @Before
    public void initTest() {
        car = createEntity(em);
    }

    @Test
    @Transactional
    public void createCar() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);
        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isCreated());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate + 1);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getLicensePlate()).isEqualTo(DEFAULT_LICENSE_PLATE);
        assertThat(testCar.getMake()).isEqualTo(DEFAULT_MAKE);
        assertThat(testCar.getModel()).isEqualTo(DEFAULT_MODEL);
        assertThat(testCar.getYear()).isEqualTo(DEFAULT_YEAR);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(1)).save(testCar);
    }

    @Test
    @Transactional
    public void createCarWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // Create the Car with an existing ID
        car.setId(1L);
        CarDTO carDTO = carMapper.toDto(car);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarMockMvc.perform(post("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    public void getAllCars() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList
        restCarMockMvc.perform(get("/api/cars?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlate").value(hasItem(DEFAULT_LICENSE_PLATE.toString())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE.toString())))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL.toString())))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR)));
    }
    
    @Test
    @Transactional
    public void getCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(car.getId().intValue()))
            .andExpect(jsonPath("$.licensePlate").value(DEFAULT_LICENSE_PLATE.toString()))
            .andExpect(jsonPath("$.make").value(DEFAULT_MAKE.toString()))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL.toString()))
            .andExpect(jsonPath("$.year").value(DEFAULT_YEAR));
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlate equals to DEFAULT_LICENSE_PLATE
        defaultCarShouldBeFound("licensePlate.equals=" + DEFAULT_LICENSE_PLATE);

        // Get all the carList where licensePlate equals to UPDATED_LICENSE_PLATE
        defaultCarShouldNotBeFound("licensePlate.equals=" + UPDATED_LICENSE_PLATE);
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlate in DEFAULT_LICENSE_PLATE or UPDATED_LICENSE_PLATE
        defaultCarShouldBeFound("licensePlate.in=" + DEFAULT_LICENSE_PLATE + "," + UPDATED_LICENSE_PLATE);

        // Get all the carList where licensePlate equals to UPDATED_LICENSE_PLATE
        defaultCarShouldNotBeFound("licensePlate.in=" + UPDATED_LICENSE_PLATE);
    }

    @Test
    @Transactional
    public void getAllCarsByLicensePlateIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where licensePlate is not null
        defaultCarShouldBeFound("licensePlate.specified=true");

        // Get all the carList where licensePlate is null
        defaultCarShouldNotBeFound("licensePlate.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make equals to DEFAULT_MAKE
        defaultCarShouldBeFound("make.equals=" + DEFAULT_MAKE);

        // Get all the carList where make equals to UPDATED_MAKE
        defaultCarShouldNotBeFound("make.equals=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make in DEFAULT_MAKE or UPDATED_MAKE
        defaultCarShouldBeFound("make.in=" + DEFAULT_MAKE + "," + UPDATED_MAKE);

        // Get all the carList where make equals to UPDATED_MAKE
        defaultCarShouldNotBeFound("make.in=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make is not null
        defaultCarShouldBeFound("make.specified=true");

        // Get all the carList where make is null
        defaultCarShouldNotBeFound("make.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model equals to DEFAULT_MODEL
        defaultCarShouldBeFound("model.equals=" + DEFAULT_MODEL);

        // Get all the carList where model equals to UPDATED_MODEL
        defaultCarShouldNotBeFound("model.equals=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model in DEFAULT_MODEL or UPDATED_MODEL
        defaultCarShouldBeFound("model.in=" + DEFAULT_MODEL + "," + UPDATED_MODEL);

        // Get all the carList where model equals to UPDATED_MODEL
        defaultCarShouldNotBeFound("model.in=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model is not null
        defaultCarShouldBeFound("model.specified=true");

        // Get all the carList where model is null
        defaultCarShouldNotBeFound("model.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByYearIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where year equals to DEFAULT_YEAR
        defaultCarShouldBeFound("year.equals=" + DEFAULT_YEAR);

        // Get all the carList where year equals to UPDATED_YEAR
        defaultCarShouldNotBeFound("year.equals=" + UPDATED_YEAR);
    }

    @Test
    @Transactional
    public void getAllCarsByYearIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where year in DEFAULT_YEAR or UPDATED_YEAR
        defaultCarShouldBeFound("year.in=" + DEFAULT_YEAR + "," + UPDATED_YEAR);

        // Get all the carList where year equals to UPDATED_YEAR
        defaultCarShouldNotBeFound("year.in=" + UPDATED_YEAR);
    }

    @Test
    @Transactional
    public void getAllCarsByYearIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where year is not null
        defaultCarShouldBeFound("year.specified=true");

        // Get all the carList where year is null
        defaultCarShouldNotBeFound("year.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByYearIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where year greater than or equals to DEFAULT_YEAR
        defaultCarShouldBeFound("year.greaterOrEqualThan=" + DEFAULT_YEAR);

        // Get all the carList where year greater than or equals to UPDATED_YEAR
        defaultCarShouldNotBeFound("year.greaterOrEqualThan=" + UPDATED_YEAR);
    }

    @Test
    @Transactional
    public void getAllCarsByYearIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where year less than or equals to DEFAULT_YEAR
        defaultCarShouldNotBeFound("year.lessThan=" + DEFAULT_YEAR);

        // Get all the carList where year less than or equals to UPDATED_YEAR
        defaultCarShouldBeFound("year.lessThan=" + UPDATED_YEAR);
    }


    @Test
    @Transactional
    public void getAllCarsByClientIsEqualToSomething() throws Exception {
        // Initialize the database
        Client client = ClientResourceIntTest.createEntity(em);
        em.persist(client);
        em.flush();
        car.setClient(client);
        carRepository.saveAndFlush(car);
        Long clientId = client.getId();

        // Get all the carList where client equals to clientId
        defaultCarShouldBeFound("clientId.equals=" + clientId);

        // Get all the carList where client equals to clientId + 1
        defaultCarShouldNotBeFound("clientId.equals=" + (clientId + 1));
    }


    @Test
    @Transactional
    public void getAllCarsByCarServiceIsEqualToSomething() throws Exception {
        // Initialize the database
        CarService carService = CarServiceResourceIntTest.createEntity(em);
        em.persist(carService);
        em.flush();
        car.addCarService(carService);
        carRepository.saveAndFlush(car);
        Long carServiceId = carService.getId();

        // Get all the carList where carService equals to carServiceId
        defaultCarShouldBeFound("carServiceId.equals=" + carServiceId);

        // Get all the carList where carService equals to carServiceId + 1
        defaultCarShouldNotBeFound("carServiceId.equals=" + (carServiceId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private void defaultCarShouldBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlate").value(hasItem(DEFAULT_LICENSE_PLATE)))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR)));

        // Check, that the count call also returns 1
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private void defaultCarShouldNotBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingCar() throws Exception {
        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car
        Car updatedCar = carRepository.findById(car.getId()).get();
        // Disconnect from session so that the updates on updatedCar are not directly saved in db
        em.detach(updatedCar);
        updatedCar
            .licensePlate(UPDATED_LICENSE_PLATE)
            .make(UPDATED_MAKE)
            .model(UPDATED_MODEL)
            .year(UPDATED_YEAR);
        CarDTO carDTO = carMapper.toDto(updatedCar);

        restCarMockMvc.perform(put("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getLicensePlate()).isEqualTo(UPDATED_LICENSE_PLATE);
        assertThat(testCar.getMake()).isEqualTo(UPDATED_MAKE);
        assertThat(testCar.getModel()).isEqualTo(UPDATED_MODEL);
        assertThat(testCar.getYear()).isEqualTo(UPDATED_YEAR);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(1)).save(testCar);
    }

    @Test
    @Transactional
    public void updateNonExistingCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Create the Car
        CarDTO carDTO = carMapper.toDto(car);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc.perform(put("/api/cars")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(carDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(0)).save(car);
    }

    @Test
    @Transactional
    public void deleteCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        int databaseSizeBeforeDelete = carRepository.findAll().size();

        // Delete the car
        restCarMockMvc.perform(delete("/api/cars/{id}", car.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Car in Elasticsearch
        verify(mockCarSearchRepository, times(1)).deleteById(car.getId());
    }

    @Test
    @Transactional
    public void searchCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);
        when(mockCarSearchRepository.search(queryStringQuery("id:" + car.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(car), PageRequest.of(0, 1), 1));
        // Search the car
        restCarMockMvc.perform(get("/api/_search/cars?query=id:" + car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].licensePlate").value(hasItem(DEFAULT_LICENSE_PLATE)))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR)));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Car.class);
        Car car1 = new Car();
        car1.setId(1L);
        Car car2 = new Car();
        car2.setId(car1.getId());
        assertThat(car1).isEqualTo(car2);
        car2.setId(2L);
        assertThat(car1).isNotEqualTo(car2);
        car1.setId(null);
        assertThat(car1).isNotEqualTo(car2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CarDTO.class);
        CarDTO carDTO1 = new CarDTO();
        carDTO1.setId(1L);
        CarDTO carDTO2 = new CarDTO();
        assertThat(carDTO1).isNotEqualTo(carDTO2);
        carDTO2.setId(carDTO1.getId());
        assertThat(carDTO1).isEqualTo(carDTO2);
        carDTO2.setId(2L);
        assertThat(carDTO1).isNotEqualTo(carDTO2);
        carDTO1.setId(null);
        assertThat(carDTO1).isNotEqualTo(carDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(carMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(carMapper.fromId(null)).isNull();
    }
}

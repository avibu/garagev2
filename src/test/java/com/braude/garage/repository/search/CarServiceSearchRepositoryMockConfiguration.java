package com.braude.garage.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of CarServiceSearchRepository to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class CarServiceSearchRepositoryMockConfiguration {

    @MockBean
    private CarServiceSearchRepository mockCarServiceSearchRepository;

}

package com.braude.garage.repository.search;

import com.braude.garage.domain.CarService;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the CarService entity.
 */
public interface CarServiceSearchRepository extends ElasticsearchRepository<CarService, Long> {
}

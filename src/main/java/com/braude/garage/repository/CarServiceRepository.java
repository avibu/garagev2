package com.braude.garage.repository;

import com.braude.garage.domain.CarService;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CarService entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CarServiceRepository extends JpaRepository<CarService, Long>, JpaSpecificationExecutor<CarService> {

}

package com.braude.garage.service;

import com.braude.garage.service.dto.ClientDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing Client.
 */
public interface ClientService {

    /**
     * Save a client.
     *
     * @param clientDTO the entity to save
     * @return the persisted entity
     */
    ClientDTO save(ClientDTO clientDTO);

    /**
     * Get all the clients.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ClientDTO> findAll(Pageable pageable);
    /**
     * Get all the ClientDTO where Car is null.
     *
     * @return the list of entities
     */
    List<ClientDTO> findAllWhereCarIsNull();


    /**
     * Get the "id" client.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<ClientDTO> findOne(Long id);

    /**
     * Delete the "id" client.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the client corresponding to the query.
     *
     * @param query the query of the search
     * 
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<ClientDTO> search(String query, Pageable pageable);
}

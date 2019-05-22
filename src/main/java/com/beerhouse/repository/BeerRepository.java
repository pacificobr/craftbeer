package com.beerhouse.repository;

import com.beerhouse.domain.Beer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Beer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BeerRepository extends JpaRepository<Beer, Integer> {

}
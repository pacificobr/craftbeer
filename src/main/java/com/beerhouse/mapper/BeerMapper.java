package com.beerhouse.mapper;

import com.beerhouse.domain.Beer;
import com.beerhouse.web.api.model.BeerDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Beer} and its DTO {@link BeerDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BeerMapper extends EntityMapper<BeerDTO, Beer> {

    //Ensure that the id will be the specified as parameter.
    default Beer toEntity(BeerDTO beerDTO, Integer id){
        beerDTO.setId(id);
        return toEntity(beerDTO);
    }

    default Beer fromId(Integer id) {
        if (id == null) {
            return null;
        }
        Beer beer = new Beer();
        beer.setId(id);
        return beer;
    }
}

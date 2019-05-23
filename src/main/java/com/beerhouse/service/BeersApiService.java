package com.beerhouse.service;

import com.beerhouse.domain.Beer;
import com.beerhouse.mapper.BeerMapper;
import com.beerhouse.repository.BeerRepository;
import com.beerhouse.util.DataUtil;
import com.beerhouse.web.api.BeersApiDelegate;
import com.beerhouse.web.api.model.BeerDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BeersApiService implements BeersApiDelegate {

    private final NativeWebRequest request;

    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper;

    //Using the constructor to easy the unitary tests
    public BeersApiService(NativeWebRequest request, BeerRepository beerRepository, BeerMapper beerMapper) {
        this.request = request;
        this.beerRepository = beerRepository;
        this.beerMapper = beerMapper;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<List<BeerDTO>> beersGet() {
        return ResponseEntity.ok(beerRepository.findAll().stream()
                .map(beerMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new)));
    }

    @Override
    public ResponseEntity<Void> beersIdDelete(String id) {
        Integer beerId = getId(id);
        if (!beerRepository.existsById(beerId)) {
            return getVoidResponse(HttpStatus.NOT_FOUND);
        }
        beerRepository.deleteById(beerId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<BeerDTO> beersIdGet(String id) {
        Integer idInt = getId(id);
        return ResponseEntity.of(beerRepository.findById(idInt)
                .map(beerMapper::toDto));
    }

    @Override
    public ResponseEntity<Void> beersIdPatch(String id, BeerDTO beer) {
        Integer beerId = getId(id);
        Optional<Beer> originalBeerOpt = beerRepository.findById(beerId);
        if (!originalBeerOpt.isPresent()) {
            return getVoidResponse(HttpStatus.NOT_FOUND);
        }
        //Avoid id manipulation
        Beer beerEntity = beerMapper.toEntity(beer, beerId);
        beerEntity = DataUtil.mergeObjects(beerEntity, originalBeerOpt.get());
        beerRepository.save(beerEntity);
        return getVoidResponse(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> beersIdPut(String id, @Valid BeerDTO beer) {
        Integer beerId = getId(id);
        if (!beerRepository.existsById(beerId)) {
            return getVoidResponse(HttpStatus.NOT_FOUND);
        }
        Beer beerEntity = beerMapper.toEntity(beer, beerId);
        beerRepository.save(beerEntity);
        return getVoidResponse(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> beersPost(@Valid BeerDTO beer) {
        Beer beerEntity = beerMapper.toEntity(beer);
        beerEntity = beerRepository.save(beerEntity);
        ResponseEntity<Void> response = getVoidResponse(beerEntity, HttpStatus.CREATED);
//        ResponseEntity<Void> response = getVoidResponse();
        return response;
    }

    private ResponseEntity<Void> getVoidResponse(HttpStatus status) {
        return getVoidResponse(null, status);
    }

    private ResponseEntity<Void> getVoidResponse(Beer beer, HttpStatus status) {
        HttpHeaders headers = new HttpHeaders();
        if (beer != null) {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(beer.getId()).toUri();
            headers.setLocation(uri);
        }
        return new ResponseEntity<>(headers, status);
    }


    private Integer getId(String id) {
        return Integer.valueOf(id);
    }
}

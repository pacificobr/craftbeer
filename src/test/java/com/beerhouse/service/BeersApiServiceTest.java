package com.beerhouse.service;

import com.beerhouse.domain.Beer;
import com.beerhouse.repository.BeerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BeersApiServiceTest {

    private static final String DEFAULT_ENDPOINT = "/beers";

    private static final String DEFAULT_NAME = "NAME_1";
    private static final String DEFAULT_INGREDIENTS = "A, B, C, D";
    private static final String DEFAULT_ALCOHOL_CONTENT = "ALCOHOL_CONTENT_1";
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1.22);
    private static final String DEFAULT_CATEGORY = "CATEGORY_1";

    private static final String UPDATED_NAME = "NAME_2";
    private static final String UPDATED_INGREDIENTS = "E, F, G, H";
    private static final String UPDATED_ALCOHOL_CONTENT = "ALCOHOL_CONTENT_2";
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2.33);
    private static final String UPDATED_CATEGORY = "CATEGORY_2";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private BeerRepository beerRepository;

    private Beer beer;

    @Before
    public void setup() {
        this.beer = createEntity();
    }

    private Beer createEntity() {
        Beer beer = new Beer();
        beer.setName(DEFAULT_NAME);
        beer.setIngredients(DEFAULT_INGREDIENTS);
        beer.setAlcoholContent(DEFAULT_ALCOHOL_CONTENT);
        beer.setPrice(DEFAULT_PRICE);
        beer.setCategory(DEFAULT_CATEGORY);
        return beer;
    }

    @Test
    @Transactional
    public void createBeer() throws Exception {

        int databaseSizeBeforeCreate = beerRepository.findAll().size();

        MvcResult mvcResult = mockMvc.perform(post(DEFAULT_ENDPOINT)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(beer)))
                .andExpect(status().isCreated()).andReturn();

        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeCreate + 1);

        Beer testBeer = beerList.get(beerList.size() - 1);
        assertThat(testBeer.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testBeer.getAlcoholContent()).isEqualTo(DEFAULT_ALCOHOL_CONTENT);
        assertThat(testBeer.getIngredients()).isEqualTo(DEFAULT_INGREDIENTS);
        assertThat(testBeer.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testBeer.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        String headerLocation = mvcResult.getResponse().getHeader("location");
        assertThat(headerLocation).endsWith(DEFAULT_ENDPOINT + "/" + testBeer.getId());
    }

    @Test
    @Transactional
    public void getAllBeers() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        // Get all the beerList
        mockMvc.perform(get(DEFAULT_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(beer.getId().intValue())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
                .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
                .andExpect(jsonPath("$.[*].alcoholContent").value(hasItem(DEFAULT_ALCOHOL_CONTENT)))
                .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE)))
                .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)));
    }

    @Test
    @Transactional
    public void getBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        // Get the beer
        mockMvc.perform(get(DEFAULT_ENDPOINT + "/{id}", beer.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(beer.getId().intValue()))
                .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
                .andExpect(jsonPath("$.ingredients").value(DEFAULT_INGREDIENTS))
                .andExpect(jsonPath("$.alcoholContent").value(DEFAULT_ALCOHOL_CONTENT))
                .andExpect(jsonPath("$.price").value(DEFAULT_PRICE))
                .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY));
    }

    @Test
    @Transactional
    public void updateBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        int databaseSizeBeforeUpdate = beerRepository.findAll().size();

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        em.detach(updatedBeer);

        updatedBeer.name(UPDATED_NAME)
                .category(UPDATED_CATEGORY)
                .alcoholContent(UPDATED_ALCOHOL_CONTENT)
                .ingredients(UPDATED_INGREDIENTS)
                .price(UPDATED_PRICE);

        mockMvc.perform(put(DEFAULT_ENDPOINT + "/{id}", beer.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedBeer)))
                .andExpect(status().isOk());

        // Validate the Category in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeUpdate);
        Beer testBeer = beerList.get(beerList.size() - 1);

        assertThat(testBeer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBeer.getIngredients()).isEqualTo(UPDATED_INGREDIENTS);
        assertThat(testBeer.getAlcoholContent()).isEqualTo(UPDATED_ALCOHOL_CONTENT);
        assertThat(testBeer.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testBeer.getCategory()).isEqualTo(UPDATED_CATEGORY);

    }

    @Test
    @Transactional
    public void patchBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        int databaseSizeBeforeUpdate = beerRepository.findAll().size();

        Beer patchedBeer = new Beer()
                .alcoholContent(UPDATED_ALCOHOL_CONTENT)
                .name(UPDATED_NAME);

        mockMvc.perform(patch(DEFAULT_ENDPOINT + "/{id}", beer.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(patchedBeer)))
                .andExpect(status().isOk());

        // Validate the Category in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeUpdate);
        Beer testBeer = beerList.get(beerList.size() - 1);

        assertThat(testBeer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testBeer.getIngredients()).isEqualTo(DEFAULT_INGREDIENTS);
        assertThat(testBeer.getAlcoholContent()).isEqualTo(UPDATED_ALCOHOL_CONTENT);
        assertThat(testBeer.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testBeer.getCategory()).isEqualTo(DEFAULT_CATEGORY);

    }

    @Test
    @Transactional
    public void deleteCategory() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        int databaseSizeBeforeDelete = beerRepository.findAll().size();

        // Delete the category
        mockMvc.perform(delete(DEFAULT_ENDPOINT + "/{id}", beer.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

        // Validate the database is empty
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
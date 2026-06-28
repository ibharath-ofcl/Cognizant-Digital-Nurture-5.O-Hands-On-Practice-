package com.cognizant.springlearn;
 
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
 
import com.cognizant.springlearn.controller.CountryController;
 
import org.springframework.security.test.context.support.WithMockUser;
 
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "user", roles = {"USER"})
class SpringLearnApplicationTests {
 
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private CountryController countryController;
 
    @Test
    void contextLoads() {
        assertNotNull(countryController);
    }
 
    @Test
    void testGetCountry() throws Exception {
        mvc.perform(get("/country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.code").value("IN"))
                .andExpect(jsonPath("$.name").value("India"));
    }
 
    @Test
    void testGetCountryException() throws Exception {
        mvc.perform(get("/countries/xyz"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Country not found"));
    }
}

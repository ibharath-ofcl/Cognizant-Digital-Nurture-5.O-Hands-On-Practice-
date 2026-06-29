package com.cognizant.centralizedauthentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CentralizedAuthenticationApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that the Spring Boot Application Context starts up successfully
    }

    @Test
    void testWelcomeEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to Centralized Authentication using OAuth2"));
    }

    @Test
    void testUserEndpoint_redirectsToLoginWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection()); // OAuth2 Login redirects to login page/provider
    }

    @Test
    void testUserEndpoint_withOAuth2MockUser() throws Exception {
        mockMvc.perform(get("/user")
                .with(SecurityMockMvcRequestPostProcessors.oauth2Login()
                        .attributes(attrs -> {
                            attrs.put("sub", "google-user-123");
                            attrs.put("name", "John Doe");
                            attrs.put("email", "john.doe@gmail.com");
                        })
                ))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sub\":\"google-user-123\",\"name\":\"John Doe\",\"email\":\"john.doe@gmail.com\"}"));
    }
}

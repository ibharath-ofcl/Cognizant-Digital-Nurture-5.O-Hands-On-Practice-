package com.cognizant.jwtauthentication;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void whenAccessingSecureEndpointWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(containsString("Full authentication is required")));
    }

    @Test
    void whenAuthenticatingWithInvalidCredentials_thenUnauthorized() throws Exception {
        String invalidPayload = "{\"username\": \"admin\", \"password\": \"wrongpass\"}";

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid username or password")));
    }

    @Test
    void whenAuthenticatingWithValidCredentials_thenReturnTokenAndAccessSecureRoutes() throws Exception {
        // 1. Authenticate and get Token
        String validPayload = "{\"username\": \"admin\", \"password\": \"password123\"}";

        MvcResult result = mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseString, Map.class);
        String token = (String) responseMap.get("token");

        // 2. Use token to access secure route /secure
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a secure endpoint."));

        // 3. Use token to access /profile
        mockMvc.perform(get("/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));

        // 4. Use token to access root status /
        mockMvc.perform(get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("JWT Authentication is working successfully."));
    }

    @Test
    void whenAccessingSecureEndpointWithInvalidToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/secure")
                        .header("Authorization", "Bearer invalidtokenhere"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized (Invalid Token)"));
    }
}

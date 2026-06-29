package com.cognizant.oauth2resourceserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.cognizant.oauth2resourceserver.config.ResourceServerConfig;
import com.cognizant.oauth2resourceserver.controller.SecureController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests validating request authorization rules and custom security filters.
 */
@WebMvcTest(SecureController.class)
@Import(ResourceServerConfig.class)
public class SecureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Mocks the JwtDecoder bean to bypass remote verification at test runtime.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    /**
     * Verifies that the root path ("/") is publicly accessible and returns the expected string.
     */
    @Test
    public void getRoot_ShouldBePublicAndReturnSuccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("OAuth2 Resource Server is running successfully"));
    }

    /**
     * Verifies that accessing "/secure" without providing a Bearer token returns 401 Unauthorized
     * formatted as a structured JSON object.
     */
    @Test
    public void getSecure_WithoutToken_ShouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(get("/secure"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/secure"));
    }

    /**
     * Verifies that accessing "/secure" with a valid mocked JWT returns 200 OK.
     */
    @Test
    public void getSecure_WithValidToken_ShouldReturn200Ok() throws Exception {
        mockMvc.perform(get("/secure")
                .with(jwt().jwt(builder -> builder.subject("user-test-1"))))
                .andExpect(status().isOk())
                .andExpect(content().string("This is a secure endpoint"));
    }

    /**
     * Verifies that the "/profile" endpoint retrieves and returns JWT claim information.
     */
    @Test
    public void getProfile_WithValidToken_ShouldReturnUserProfile() throws Exception {
        mockMvc.perform(get("/profile")
                .with(jwt().jwt(builder -> builder
                        .subject("john.doe@cognizant.com")
                        .claim("scope", "read write")
                        .claim("company", "Cognizant")
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("john.doe@cognizant.com"))
                .andExpect(jsonPath("$.claims.company").value("Cognizant"))
                .andExpect(jsonPath("$.claims.scope").value("read write"));
    }
}

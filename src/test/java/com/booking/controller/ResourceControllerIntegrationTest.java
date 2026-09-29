package com.booking.controller;

import com.booking.dto.request.LoginRequest;
import com.booking.dto.request.ResourceRequest;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        reservationRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        userRepository.save(User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).build());

        userRepository.save(User.builder()
                .username("user").email("user@test.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).build());

        // Get tokens
        adminToken = getToken("admin", "admin123");
        userToken = getToken("user", "user123");
    }

    private String getToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return "Bearer " + objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("ADMIN should create resource")
    void adminShouldCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Room X", "A nice room", "ROOM", true);

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Room X"))
                .andExpect(jsonPath("$.type").value("ROOM"));
    }

    @Test
    @DisplayName("USER should NOT create resource")
    void userShouldNotCreateResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Room X", "A nice room", "ROOM", true);

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER should read resources")
    void userShouldReadResources() throws Exception {
        // Admin creates a resource first
        ResourceRequest request = new ResourceRequest("Room Y", "Another room", "ROOM", true);
        mockMvc.perform(post("/api/resources")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // User reads resources
        mockMvc.perform(get("/api/resources")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Unauthenticated should NOT access resources")
    void unauthenticatedShouldNotAccessResources() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN should update resource")
    void adminShouldUpdateResource() throws Exception {
        // Create resource
        ResourceRequest createRequest = new ResourceRequest("Room Z", "Old desc", "ROOM", true);
        MvcResult createResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long resourceId = objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update resource
        ResourceRequest updateRequest = new ResourceRequest("Room Z Updated", "New desc", "ROOM", false);
        mockMvc.perform(put("/api/resources/" + resourceId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Room Z Updated"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("ADMIN should delete resource")
    void adminShouldDeleteResource() throws Exception {
        ResourceRequest request = new ResourceRequest("To Delete", "Temp", "EQUIPMENT", true);
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long resourceId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/resources/" + resourceId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/resources/" + resourceId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("USER should NOT delete resource")
    void userShouldNotDeleteResource() throws Exception {
        ResourceRequest request = new ResourceRequest("Protected", "Don't delete", "ROOM", true);
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long resourceId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/resources/" + resourceId)
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }
}

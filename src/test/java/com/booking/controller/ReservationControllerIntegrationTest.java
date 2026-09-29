package com.booking.controller;

import com.booking.dto.request.LoginRequest;
import com.booking.dto.request.ReservationRequest;
import com.booking.dto.request.ResourceRequest;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;
    private String adminToken;
    private String userToken;
    private String user2Token;
    private Long resourceId;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        reservationRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        // Create users
        userRepository.save(User.builder()
                .username("admin").email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN).build());

        userRepository.save(User.builder()
                .username("user1").email("user1@test.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).build());

        userRepository.save(User.builder()
                .username("user2").email("user2@test.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER).build());

        adminToken = getToken("admin", "admin123");
        userToken = getToken("user1", "user123");
        user2Token = getToken("user2", "user123");

        // Create a resource for reservations
        ResourceRequest resourceRequest = new ResourceRequest("Room A", "Test room", "ROOM", true);
        MvcResult result = mockMvc.perform(post("/api/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        resourceId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();
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
    @DisplayName("USER should create reservation with identity from JWT")
    void userShouldCreateReservation() throws Exception {
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("150.00"));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.price").value(150.00));
    }

    @Test
    @DisplayName("Should reject reservation with invalid time range")
    void shouldRejectInvalidTimeRange() throws Exception {
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("USER should see only their own reservations")
    void userShouldSeeOnlyOwnReservations() throws Exception {
        // User1 creates a reservation
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // User2 should see empty results
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        // User1 should see their reservation
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("user1"));
    }

    @Test
    @DisplayName("ADMIN should see all reservations")
    void adminShouldSeeAllReservations() throws Exception {
        // User1 creates reservation
        ReservationRequest req1 = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        // User2 creates reservation
        ReservationRequest req2 = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                new BigDecimal("200.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        // Admin sees all
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("Should filter reservations by status")
    void shouldFilterByStatus() throws Exception {
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Filter by PENDING — should find it
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // Filter by CONFIRMED — should not find it
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should filter reservations by price range")
    void shouldFilterByPriceRange() throws Exception {
        // Create two reservations with different prices
        ReservationRequest cheap = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("50.00"));

        ReservationRequest expensive = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                new BigDecimal("500.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cheap)));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expensive)));

        // Filter by minPrice=100 should return only expensive
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken)
                        .param("minPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].price").value(500.00));
    }

    @Test
    @DisplayName("Should support pagination")
    void shouldSupportPagination() throws Exception {
        // Create 3 reservations
        for (int i = 1; i <= 3; i++) {
            ReservationRequest req = new ReservationRequest(
                    resourceId,
                    LocalDateTime.now().plusDays(i),
                    LocalDateTime.now().plusDays(i).plusHours(1),
                    new BigDecimal(i * 100));

            mockMvc.perform(post("/api/reservations")
                    .header("Authorization", userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));
        }

        // Get page 0 with size 2
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("Should support sorting")
    void shouldSupportSorting() throws Exception {
        ReservationRequest req1 = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("300.00"));

        ReservationRequest req2 = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        // Sort by price ascending
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", userToken)
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(100.00))
                .andExpect(jsonPath("$.content[1].price").value(300.00));
    }

    @Test
    @DisplayName("USER should NOT access another user's reservation by ID")
    void userShouldNotAccessOtherUserReservation() throws Exception {
        // User1 creates reservation
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("100.00"));

        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long reservationId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // User2 tries to access it
        mockMvc.perform(get("/api/reservations/" + reservationId)
                        .header("Authorization", user2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Only ADMIN should delete reservations")
    void onlyAdminShouldDeleteReservations() throws Exception {
        ReservationRequest request = new ReservationRequest(
                resourceId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("100.00"));

        MvcResult result = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long reservationId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();

        // User tries to delete — forbidden
        mockMvc.perform(delete("/api/reservations/" + reservationId)
                        .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        // Admin deletes — success
        mockMvc.perform(delete("/api/reservations/" + reservationId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }
}

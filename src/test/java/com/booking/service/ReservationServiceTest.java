package com.booking.service;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.request.ReservationUpdateRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.*;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private User otherUser;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("user").email("user@test.com")
                .password("encoded").role(Role.USER).build();

        otherUser = User.builder()
                .id(2L).username("other").email("other@test.com")
                .password("encoded").role(Role.USER).build();

        resource = Resource.builder()
                .id(1L).name("Room A").description("Test room")
                .type("ROOM").available(true).build();

        reservation = Reservation.builder()
                .id(1L).user(user).resource(resource)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(ReservationStatus.PENDING)
                .price(new BigDecimal("99.99"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create reservation with user from JWT")
    void shouldCreateReservation() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                new BigDecimal("99.99"));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.createReservation(request, "user");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw when start time is after end time")
    void shouldThrowWhenInvalidTimeRange() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusHours(3),
                LocalDateTime.now().plusHours(1),
                new BigDecimal("50.00"));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        assertThatThrownBy(() -> reservationService.createReservation(request, "user"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    @DisplayName("Should throw when resource not found for reservation")
    void shouldThrowWhenResourceNotFoundForReservation() {
        ReservationRequest request = new ReservationRequest(
                99L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                new BigDecimal("50.00"));

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(request, "user"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should allow user to view their own reservation")
    void shouldAllowUserToViewOwnReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, "user", false);

        assertThat(response.getUsername()).isEqualTo("user");
    }

    @Test
    @DisplayName("Should deny user from viewing another user's reservation")
    void shouldDenyUserFromViewingOtherReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.getReservationById(1L, "other", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Should allow admin to view any reservation")
    void shouldAllowAdminToViewAnyReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, "admin", true);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should allow user to update their own reservation")
    void shouldAllowUserToUpdateOwnReservation() {
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest();
        updateRequest.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse response = reservationService.updateReservation(
                1L, updateRequest, "user", false);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Should deny user from updating another user's reservation")
    void shouldDenyUserFromUpdatingOtherReservation() {
        ReservationUpdateRequest updateRequest = new ReservationUpdateRequest();
        updateRequest.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() ->
                reservationService.updateReservation(1L, updateRequest, "other", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Should delete reservation")
    void shouldDeleteReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L);

        verify(reservationRepository).delete(reservation);
    }
}

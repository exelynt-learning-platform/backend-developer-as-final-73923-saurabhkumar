package com.booking.service;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.request.ReservationUpdateRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.exception.BadRequestException;
import com.booking.exception.ReservationConflictException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.*;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.specification.ReservationSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ResourceRepository resourceRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new BadRequestException("Start time must be before end time");
        }
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource", "id", request.getResourceId()));

        if (!resource.getAvailable()) {
            throw new BadRequestException("Resource is not available for reservation");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (reservationRepository.existsByResourceIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
                resource.getId(), ReservationStatus.CANCELLED, request.getEndTime(), request.getStartTime())) {
            throw new ReservationConflictException("Resource is already reserved for the given time slot");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING)
                .price(request.getPrice())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String username,
            boolean isAdmin,
            Pageable pageable) {

        Specification<Reservation> spec = Specification.where(
                ReservationSpecification.hasStatus(status))
                .and(ReservationSpecification.hasMinPrice(minPrice))
                .and(ReservationSpecification.hasMaxPrice(maxPrice));

        // USER can only see their own reservations
        if (!isAdmin) {
            spec = spec.and(ReservationSpecification.belongsToUsername(username));
        }

        return reservationRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, String username, boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Check ownership for non-admin users
        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException(
                    "You don't have permission to access this reservation");
        }

        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id,
                                                  ReservationUpdateRequest request,
                                                  String username,
                                                  boolean isAdmin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));

        // Check ownership for non-admin users
        if (!isAdmin && !reservation.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException(
                    "You don't have permission to update this reservation");
        }

        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }
        if (request.getStartTime() != null) {
            reservation.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            reservation.setEndTime(request.getEndTime());
        }
        if (request.getPrice() != null) {
            reservation.setPrice(request.getPrice());
        }

        // Always validate start < end regardless of status
        validateTimeRange(reservation.getStartTime(), reservation.getEndTime());

        Reservation updated = reservationRepository.save(reservation);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", id));
                
        reservationRepository.delete(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .username(reservation.getUser().getUsername())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .resourceType(reservation.getResource().getType())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .price(reservation.getPrice())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}

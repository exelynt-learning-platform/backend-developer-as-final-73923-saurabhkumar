package com.booking.controller;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.request.ReservationUpdateRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.model.ReservationStatus;
import com.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "Get reservations",
            description = "Get paginated reservations. ADMIN sees all, USER sees only their own. " +
                    "Supports filtering by status, minPrice, maxPrice and sorting.")
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @Parameter(description = "Filter by reservation status")
            @RequestParam(required = false) ReservationStatus status,
            @Parameter(description = "Filter by minimum price")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Filter by maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {

        if (pageable.getSort().isSorted()) {
            java.util.List<String> allowedSorts = java.util.Arrays.asList(
                    "price", "startTime", "endTime", "status", "createdAt", "updatedAt", "id"
            );
            for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                if (!allowedSorts.contains(order.getProperty())) {
                    throw new com.booking.exception.BadRequestException("Invalid sort property: " + order.getProperty());
                }
            }
        }

        Page<ReservationResponse> reservations = reservationService.getReservations(
                status, minPrice, maxPrice, pageable);

        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID",
            description = "Get a single reservation. ADMIN can access any, USER can access only their own.")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PostMapping
    @Operation(summary = "Create reservation",
            description = "Create a new reservation. User identity is taken from JWT token.")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation",
            description = "Update a reservation. ADMIN can update any, USER can update only their own.")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation", description = "Delete a reservation (ADMIN only)")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}

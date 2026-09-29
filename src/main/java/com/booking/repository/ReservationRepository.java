package com.booking.repository;

import com.booking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.booking.model.ReservationStatus;
import java.time.LocalDateTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {
    
    boolean existsByResourceIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long resourceId, 
            ReservationStatus status, 
            LocalDateTime endTime, 
            LocalDateTime startTime);
}

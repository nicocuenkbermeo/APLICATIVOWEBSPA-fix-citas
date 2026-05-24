package com.spa.bookingapp.repository;

import com.spa.bookingapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByTherapistId(Long therapistId);
    
    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(a) FROM Appointment a WHERE a.therapist.id = :therapistId " +
        "AND a.status <> 'CANCELLED' " +
        "AND a.appointmentEndTime IS NOT NULL " +
        "AND a.appointmentTime < :endTime " +
        "AND a.appointmentEndTime > :startTime"
    )
    long countOverlappingAppointments(
        @org.springframework.data.repository.query.Param("therapistId") Long therapistId,
        @org.springframework.data.repository.query.Param("startTime") java.time.LocalDateTime startTime,
        @org.springframework.data.repository.query.Param("endTime") java.time.LocalDateTime endTime
    );
}

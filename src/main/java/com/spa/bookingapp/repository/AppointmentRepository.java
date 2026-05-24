package com.spa.bookingapp.repository;

import com.spa.bookingapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findByTherapistId(Long therapistId);

    // Detección de solapamiento clásico entre intervalos [A.start, A.end) y [B.start, B.end):
    //   se solapan  <=>  A.start < B.end  &&  A.end > B.start
    // Se excluyen estados cancelados (variantes EN/ES) y citas legacy sin fin definido.
    @Query(
        "SELECT COUNT(a) FROM Appointment a " +
        "WHERE a.therapist.id = :therapistId " +
        "AND a.status NOT IN ('CANCELLED', 'CANCELED', 'CANCELADA') " +
        "AND a.appointmentEndTime IS NOT NULL " +
        "AND a.appointmentTime < :endTime " +
        "AND a.appointmentEndTime > :startTime"
    )
    long countOverlappingAppointments(
        @Param("therapistId") Long therapistId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    // Devuelve las citas que solapan, ordenadas por hora de inicio.
    // Se usa para construir mensajes de error con la próxima franja libre.
    @Query(
        "SELECT a FROM Appointment a " +
        "WHERE a.therapist.id = :therapistId " +
        "AND a.status NOT IN ('CANCELLED', 'CANCELED', 'CANCELADA') " +
        "AND a.appointmentEndTime IS NOT NULL " +
        "AND a.appointmentTime < :endTime " +
        "AND a.appointmentEndTime > :startTime " +
        "ORDER BY a.appointmentTime ASC"
    )
    List<Appointment> findOverlappingAppointments(
        @Param("therapistId") Long therapistId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

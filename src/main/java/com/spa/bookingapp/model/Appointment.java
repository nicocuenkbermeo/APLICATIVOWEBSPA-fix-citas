package com.spa.bookingapp.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private User therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private SpaService service;

    private LocalDateTime appointmentTime;
    
    private LocalDateTime appointmentEndTime;
    
    private String status; // PENDING, CONFIRMED, CANCELLED

    public Appointment() {}

    public Appointment(User client, User therapist, SpaService service, LocalDateTime appointmentTime, LocalDateTime appointmentEndTime, String status) {
        this.client = client;
        this.therapist = therapist;
        this.service = service;
        this.appointmentTime = appointmentTime;
        this.appointmentEndTime = appointmentEndTime;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public User getTherapist() { return therapist; }
    public void setTherapist(User therapist) { this.therapist = therapist; }

    public SpaService getService() { return service; }
    public void setService(SpaService service) { this.service = service; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public LocalDateTime getAppointmentEndTime() { return appointmentEndTime; }
    public void setAppointmentEndTime(LocalDateTime appointmentEndTime) { this.appointmentEndTime = appointmentEndTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

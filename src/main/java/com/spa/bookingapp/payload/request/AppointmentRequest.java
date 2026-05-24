package com.spa.bookingapp.payload.request;

import java.time.LocalDateTime;

public class AppointmentRequest {
    private Long serviceId;
    private Long therapistId;
    private String appointmentTime;

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getTherapistId() { return therapistId; }
    public void setTherapistId(Long therapistId) { this.therapistId = therapistId; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
}

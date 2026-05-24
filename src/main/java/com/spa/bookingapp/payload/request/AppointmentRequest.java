package com.spa.bookingapp.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AppointmentRequest {
    @NotNull(message = "El servicio es obligatorio")
    private Long serviceId;

    @NotNull(message = "El terapeuta es obligatorio")
    private Long therapistId;

    @NotBlank(message = "La fecha y hora son obligatorias")
    private String appointmentTime;

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getTherapistId() { return therapistId; }
    public void setTherapistId(Long therapistId) { this.therapistId = therapistId; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
}

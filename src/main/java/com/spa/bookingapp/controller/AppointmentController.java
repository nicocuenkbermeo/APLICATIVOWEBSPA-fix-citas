package com.spa.bookingapp.controller;

import com.spa.bookingapp.model.Appointment;
import com.spa.bookingapp.model.SpaService;
import com.spa.bookingapp.model.User;
import com.spa.bookingapp.payload.request.AppointmentRequest;
import com.spa.bookingapp.repository.AppointmentRepository;
import com.spa.bookingapp.repository.SpaServiceRepository;
import com.spa.bookingapp.repository.UserRepository;
import com.spa.bookingapp.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SpaServiceRepository spaServiceRepository;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        User client = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Client not found."));

        User therapist = userRepository.findById(request.getTherapistId())
                .orElseThrow(() -> new RuntimeException("Error: Therapist not found."));

        SpaService service = spaServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Error: Service not found."));

        LocalDateTime parsedDate;
        try {
            // Manejar si el string viene sin segundos
            String dateStr = request.getAppointmentTime();
            if (dateStr.length() == 16) {
                dateStr += ":00";
            }
            parsedDate = LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Formato de fecha inválido.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Validar que la cita no se agende en el pasado
        if (!parsedDate.isAfter(LocalDateTime.now())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "No se puede agendar una cita en el pasado. Elegí una fecha y hora futura.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Calcular la hora de fin usando la duración del servicio
        LocalDateTime endTime = parsedDate.plusMinutes(service.getDurationMinutes());

        // Validar que no haya cruce de horarios para ese terapeuta (superposición de bloques)
        List<Appointment> overlaps = appointmentRepository.findOverlappingAppointments(
                therapist.getId(), parsedDate, endTime);

        if (!overlaps.isEmpty()) {
            // Sugerir la próxima franja libre = fin de la última cita que solapa
            LocalDateTime nextAvailable = overlaps.get(0).getAppointmentEndTime();
            for (Appointment a : overlaps) {
                if (a.getAppointmentEndTime().isAfter(nextAvailable)) {
                    nextAvailable = a.getAppointmentEndTime();
                }
            }

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message",
                    "Esa franja horaria ya está reservada para este terapeuta. " +
                    "Disponibilidad más cercana: " + nextAvailable.toString());
            errorResponse.put("nextAvailable", nextAvailable.toString());
            errorResponse.put("conflicts", overlaps.size());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setTherapist(therapist);
        appointment.setService(service);
        appointment.setAppointmentTime(parsedDate);
        appointment.setAppointmentEndTime(endTime);
        appointment.setStatus("PENDING");

        appointmentRepository.save(appointment);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Appointment created successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/therapist")
    @PreAuthorize("hasRole('THERAPIST') or hasRole('ADMIN')")
    public ResponseEntity<?> getTherapistAppointments() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appointment> appointments = appointmentRepository.findByTherapistId(userDetails.getId());
        
        List<Map<String, Object>> response = appointments.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("clientName", a.getClient().getName());
            map.put("serviceName", a.getService().getName());
            map.put("appointmentTime", a.getAppointmentTime());
            map.put("status", a.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        
        List<Map<String, Object>> response = appointments.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("clientName", a.getClient().getName());
            map.put("therapistName", a.getTherapist().getName());
            map.put("serviceName", a.getService().getName());
            map.put("appointmentTime", a.getAppointmentTime());
            map.put("status", a.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getClientAppointments() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Appointment> appointments = appointmentRepository.findByClientId(userDetails.getId());
        
        List<Map<String, Object>> response = appointments.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("therapistName", a.getTherapist().getName());
            map.put("serviceName", a.getService().getName());
            map.put("appointmentTime", a.getAppointmentTime());
            map.put("status", a.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugAppointments() {
        List<Appointment> all = appointmentRepository.findAll();
        List<Map<String, Object>> response = all.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("therapistId", a.getTherapist().getId());
            map.put("therapistName", a.getTherapist().getName());
            map.put("startTime", a.getAppointmentTime());
            map.put("endTime", a.getAppointmentEndTime());
            map.put("status", a.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
}

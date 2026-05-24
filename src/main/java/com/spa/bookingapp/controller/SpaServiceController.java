package com.spa.bookingapp.controller;

import com.spa.bookingapp.model.SpaService;
import com.spa.bookingapp.repository.SpaServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/services")
public class SpaServiceController {

    @Autowired
    SpaServiceRepository spaServiceRepository;

    @GetMapping
    public List<SpaService> getAllServices() {
        return spaServiceRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createService(@RequestBody SpaService service) {
        spaServiceRepository.save(service);
        return ResponseEntity.ok("Service created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody SpaService updatedService) {
        Optional<SpaService> existingService = spaServiceRepository.findById(id);
        if (existingService.isPresent()) {
            SpaService service = existingService.get();
            service.setName(updatedService.getName());
            service.setDescription(updatedService.getDescription());
            service.setPrice(updatedService.getPrice());
            service.setCategory(updatedService.getCategory());
            service.setImageUrl(updatedService.getImageUrl());
            spaServiceRepository.save(service);
            return ResponseEntity.ok("Service updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        if (spaServiceRepository.existsById(id)) {
            spaServiceRepository.deleteById(id);
            return ResponseEntity.ok("Service deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

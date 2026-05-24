package com.spa.bookingapp.controller;

import com.spa.bookingapp.model.User;
import com.spa.bookingapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/therapists")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public List<Map<String, Object>> getTherapists() {
        List<User> therapists = userRepository.findByRoles_Name("ROLE_THERAPIST");
        return therapists.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("username", t.getUsername());
            return map;
        }).collect(Collectors.toList());
    }
}

package com.spa.bookingapp.security;

import com.spa.bookingapp.model.Role;
import com.spa.bookingapp.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!roleRepository.findByName("ROLE_CLIENT").isPresent()) {
            roleRepository.save(new Role("ROLE_CLIENT"));
        }
        if (!roleRepository.findByName("ROLE_THERAPIST").isPresent()) {
            roleRepository.save(new Role("ROLE_THERAPIST"));
        }
        if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
    }
}

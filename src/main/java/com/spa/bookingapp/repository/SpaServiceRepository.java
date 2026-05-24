package com.spa.bookingapp.repository;

import com.spa.bookingapp.model.SpaService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaServiceRepository extends JpaRepository<SpaService, Long> {
}

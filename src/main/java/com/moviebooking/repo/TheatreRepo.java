package com.moviebooking.repo;

import com.moviebooking.entity.Theatre;
import com.moviebooking.entity.enums.TheatreStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheatreRepo extends JpaRepository<Theatre, Long> {

    // Active theatres
    Page<Theatre> findByTheatreStatus(
            TheatreStatus theatreStatus,
            Pageable pageable
    );

    // Active theatres filtered by city
    Page<Theatre> findByCityIgnoreCaseAndTheatreStatus(
            String city,
            TheatreStatus theatreStatus,
            Pageable pageable
    );

    // All theatres filtered by city - Admin
    Page<Theatre> findByCityIgnoreCase(
            String city,
            Pageable pageable
    );
}
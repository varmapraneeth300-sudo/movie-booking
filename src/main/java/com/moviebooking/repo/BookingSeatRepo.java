package com.moviebooking.repo;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepo extends JpaRepository<BookingSeat, Long> {
    List<BookingSeat> findByBooking(Booking booking);
}

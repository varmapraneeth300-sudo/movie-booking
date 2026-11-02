package com.moviebooking.repo;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByShowIn(List<Show> shows);

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    List<Booking> findByShow(Show show);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);
}
package com.moviebooking.repo;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepo extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);

    List<Seat> findByScreen(Screen screen);
    boolean existsByScreen(Screen screen);
    boolean existsByScreenAndRowLabelAndSeatNumber(
            Screen screen,
            String rowLabel,
            Integer seatNumber
    );
}
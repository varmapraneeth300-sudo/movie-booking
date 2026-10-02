package com.moviebooking.repo;

import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepo extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findBySeat(Seat seat);
    List<ShowSeat> findByShow(Show show);

    boolean existsByShow(Show show);

    ShowSeat findByShowAndSeat(Show show, Seat seat);

    Optional<ShowSeat> findByShowAndSeatId(
            Show show,
            Long seatId
    );
}

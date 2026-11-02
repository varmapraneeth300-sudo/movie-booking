package com.moviebooking.repo;

import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepo extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findBySeat(Seat seat);

    List<ShowSeat> findByShow(Show show);

    boolean existsByShow(Show show);

    ShowSeat findByShowAndSeat(Show show, Seat seat);

    Optional<ShowSeat> findByShowAndSeatId(Show show, Long seatId);

    List<ShowSeat> findByShowAndHeldByUserIdAndStatus(Show show, Long id, ShowSeatStatus showSeatStatus);


    @Query(value = """
            
                SELECT DISTINCT ss.*
            FROM show_seats ss
            JOIN booking_seats bs ON ss.id = bs.show_seat_id
            JOIN bookings b ON bs.booking_id = b.id
            WHERE b.status IN ('EXPIRED', 'FAILED', 'PAYMENT_UNRESOLVED')
              AND ss.status = 'HELD'
              AND ss.hold_expires_at <= NOW()
            """, nativeQuery = true)
    List<ShowSeat> findExpiredHeldSeats();
}

package com.moviebooking.scheduler;

import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.enums.ShowSeatStatus;
import com.moviebooking.repo.ShowSeatRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatHoldScheduler {

    private final ShowSeatRepo showSeatRepo;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredSeats() {

        List<ShowSeat> expiredSeats =
                showSeatRepo.findExpiredHeldSeats();

        for (ShowSeat showSeat : expiredSeats) {

            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeat.setHeldByUserId(null);
            showSeat.setHoldExpiresAt(null);
            showSeat.setHoldId(null);
        }
    }
}

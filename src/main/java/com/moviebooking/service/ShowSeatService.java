package com.moviebooking.service;

import com.moviebooking.dto.ShowSeatResponse;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.enums.SeatType;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.ShowRepo;
import com.moviebooking.repo.ShowSeatRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowSeatService {
    private final ShowSeatRepo showSeatRepo;
    private final ShowRepo showRepo;

    ShowSeatService(ShowSeatRepo showSeatRepo, ShowRepo showRepo) {
        this.showSeatRepo = showSeatRepo;
        this.showRepo = showRepo;
    }

    public List<ShowSeatResponse> getShowSeats(Long showId) {

        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new ApplicationException(
                        "Show not found",
                        HttpStatus.NOT_FOUND
                ));

        return showSeatRepo.findByShow(show)
                .stream()
                .map(this::mapToShowSeatResponse)
                .toList();
    }

    private ShowSeatResponse mapToShowSeatResponse(ShowSeat showSeat) {

        Seat seat = showSeat.getSeat();

        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getShow().getId(),
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                (int) showSeat.getPrice(),
                showSeat.getStatus()
        );
    }
}

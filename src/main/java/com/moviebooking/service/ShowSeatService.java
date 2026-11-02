package com.moviebooking.service;

import com.moviebooking.dto.ShowSeatResponse;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.ShowSeat;
import com.moviebooking.entity.User;
import com.moviebooking.entity.enums.SeatType;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.ShowRepo;
import com.moviebooking.repo.ShowSeatRepo;
import com.moviebooking.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ShowSeatService {
    private final ShowSeatRepo showSeatRepo;
    private final ShowRepo showRepo;
    private final UserRepo userRepo;

    ShowSeatService(ShowSeatRepo showSeatRepo, ShowRepo showRepo, UserRepo userRepo) {
        this.showSeatRepo = showSeatRepo;
        this.showRepo = showRepo;
        this.userRepo = userRepo;
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

    User getUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();
        return userRepo.findByUsername(username).orElseThrow(() -> new ApplicationException("user not found", HttpStatus.NOT_FOUND));
    }

    private ShowSeatResponse mapToShowSeatResponse(ShowSeat showSeat) {

        Seat seat = showSeat.getSeat();
        User user = getUser();

        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getShow().getId(),
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                (int) showSeat.getPrice(),
                showSeat.getStatus(),
                Objects.equals(showSeat.getHeldByUserId(), user.getId())
        );
    }
}

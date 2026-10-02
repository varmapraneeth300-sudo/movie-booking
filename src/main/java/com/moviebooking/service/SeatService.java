package com.moviebooking.service;

import com.moviebooking.dto.SeatRequest;
import com.moviebooking.dto.SeatResponse;
import com.moviebooking.dto.SeatStatusResponse;
import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Seat;
import com.moviebooking.entity.enums.ScreenStatus;
import com.moviebooking.entity.enums.SeatStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.ScreenRepo;
import com.moviebooking.repo.SeatRepo;
import com.moviebooking.repo.ShowSeatRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    private final ScreenRepo screenRepo;
    private final SeatRepo seatRepo;
    private final ShowSeatRepo showSeatRepo;

    SeatService(ScreenRepo screenRepo, SeatRepo seatRepo, ShowSeatRepo showSeatRepo) {
        this.screenRepo = screenRepo;
        this.seatRepo = seatRepo;
        this.showSeatRepo = showSeatRepo;
    }

    @Transactional
    public SeatResponse createSeat(
            Long screenId,
            SeatRequest request) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        if (screen.getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot create seat for an inactive screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean exists = seatRepo.existsByScreenAndRowLabelAndSeatNumber(
                screen,
                request.getRowLabel(),
                request.getSeatNumber()
        );

        if (exists) {
            throw new ApplicationException(
                    "Seat " + request.getRowLabel() + request.getSeatNumber()
                            + " already exists in this screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        Seat seat = new Seat();

        seat.setScreen(screen);
        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setSeatStatus(SeatStatus.ACTIVE);

        Seat savedSeat = seatRepo.save(seat);

        return new SeatResponse(
                savedSeat.getId(),
                savedSeat.getScreen().getId(),
                savedSeat.getRowLabel(),
                savedSeat.getSeatNumber(),
                savedSeat.getSeatStatus(),
                savedSeat.getSeatType()
        );
    }

    public List<SeatResponse> getSeatsByScreen(Long screenId) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        return seatRepo.findByScreenId(screenId)
                .stream()
                .map(this::mapToSeatResponse)
                .toList();
    }

    private SeatResponse mapToSeatResponse(Seat seat) {

        return new SeatResponse(
                seat.getId(),
                seat.getScreen().getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatStatus(),
                seat.getSeatType()
        );
    }

    public SeatResponse getSeatById(Long seatId) {

        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new ApplicationException(
                        "Seat not found with id: " + seatId,
                        HttpStatus.NOT_FOUND
                ));

        return mapToSeatResponse(seat);
    }

    @Transactional
    public SeatResponse updateSeat(
            Long seatId,
            SeatRequest request) {

        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new ApplicationException(
                        "Seat not found with id: " + seatId,
                        HttpStatus.NOT_FOUND
                ));

        if (seat.getScreen().getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot update a seat belonging to an inactive screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());

        return mapToSeatResponse(seat);
    }

    @Transactional
    public List<SeatResponse> deleteSeat(long seatId) {
        Seat seat = seatRepo.findById(seatId).orElseThrow(() -> new ApplicationException("seat not found", HttpStatus.NOT_FOUND));
        showSeatRepo.findBySeat(seat);
        return new ArrayList<>();
    }

    @Transactional
    public SeatStatusResponse deleteSeat(Long seatId) {

        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new ApplicationException(
                        "Seat not found with id: " + seatId,
                        HttpStatus.NOT_FOUND
                ));

        if (seat.getSeatStatus() == SeatStatus.INACTIVE) {
            throw new ApplicationException(
                    "Seat is already inactive",
                    HttpStatus.BAD_REQUEST
            );
        }

        seat.setSeatStatus(SeatStatus.INACTIVE);

        return new SeatStatusResponse(
                seat.getId(),
                seat.getSeatStatus(),
                "Seat deactivated successfully"
        );
    }

    @Transactional
    public SeatStatusResponse activateSeat(Long seatId) {

        Seat seat = seatRepo.findById(seatId)
                .orElseThrow(() -> new ApplicationException(
                        "Seat not found with id: " + seatId,
                        HttpStatus.NOT_FOUND
                ));

        if (seat.getSeatStatus() == SeatStatus.ACTIVE) {
            throw new ApplicationException(
                    "Seat is already active",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (seat.getScreen().getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot activate a seat belonging to an inactive screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        seat.setSeatStatus(SeatStatus.ACTIVE);

        return new SeatStatusResponse(
                seat.getId(),
                seat.getSeatStatus(),
                "Seat activated successfully"
        );
    }
}

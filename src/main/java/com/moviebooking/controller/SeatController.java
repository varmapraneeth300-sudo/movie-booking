package com.moviebooking.controller;

import com.moviebooking.dto.SeatRequest;
import com.moviebooking.dto.SeatResponse;
import com.moviebooking.dto.SeatStatusResponse;
import com.moviebooking.dto.ShowSeatResponse;
import com.moviebooking.service.SeatService;
import com.moviebooking.service.ShowSeatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;
    private final ShowSeatService showSeatService;

    SeatController(
            SeatService seatService,
            ShowSeatService showSeatService) {

        this.seatService = seatService;
        this.showSeatService = showSeatService;
    }


    // =========================================================
    // SCREEN SEATS - ADMIN
    // =========================================================

    @PostMapping("/screens/{screenId}/seats")
    public SeatResponse createSeat(
            @PathVariable Long screenId,
            @Valid @RequestBody SeatRequest request) {

        return seatService.createSeat(screenId, request);
    }


    @GetMapping("/screens/{screenId}/seats")
    public List<SeatResponse> getSeatsByScreen(
            @PathVariable Long screenId) {

        return seatService.getSeatsByScreen(screenId);
    }


    @GetMapping("/seats/{seatId}")
    public SeatResponse getSeatById(
            @PathVariable Long seatId) {

        return seatService.getSeatById(seatId);
    }


    @PutMapping("/seats/{seatId}")
    public SeatResponse updateSeat(
            @PathVariable Long seatId,
            @Valid @RequestBody SeatRequest request) {

        return seatService.updateSeat(seatId, request);
    }


    @DeleteMapping("/seats/{seatId}")
    public SeatStatusResponse deleteSeat(
            @PathVariable Long seatId) {

        return seatService.deleteSeat(seatId);
    }


    @PatchMapping("/seats/{seatId}/activate")
    public SeatStatusResponse activateSeat(
            @PathVariable Long seatId) {

        return seatService.activateSeat(seatId);
    }


    // =========================================================
    // SHOW SEATS - CUSTOMER
    // =========================================================

    @GetMapping("/shows/{showId}/seats")
    public List<ShowSeatResponse> getShowSeats(
            @PathVariable Long showId) {

        return showSeatService.getShowSeats(showId);
    }
}
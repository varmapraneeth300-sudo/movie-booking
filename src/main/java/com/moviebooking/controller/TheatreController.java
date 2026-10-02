package com.moviebooking.controller;

import com.moviebooking.dto.DeleteTheatreResponse;
import com.moviebooking.dto.TheatreRequest;
import com.moviebooking.dto.TheatreResponse;
import com.moviebooking.dto.TheatreStatusResponse;
import com.moviebooking.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    // Public - active theatres
    @GetMapping
    public List<TheatreResponse> getTheatres(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return theatreService.getTheatres(city, page, size);
    }

    // Admin - all theatres
    @GetMapping("/admin")
    public List<TheatreResponse> getAllTheatres(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return theatreService.getAllTheatres(city, page, size);
    }

    // Get theatre details
    @GetMapping("/{theatreId}")
    public TheatreResponse getTheatreById(
            @PathVariable Long theatreId) {

        return theatreService.getTheatreById(theatreId);
    }

    // Admin - create
    @PostMapping
    public TheatreResponse createTheatre(
            @Valid @RequestBody TheatreRequest request) {

        return theatreService.createTheatre(request);
    }

    // Admin - update
    @PutMapping("/{theatreId}")
    public TheatreResponse updateTheatre(
            @PathVariable Long theatreId,
            @Valid @RequestBody TheatreRequest request) {

        return theatreService.updateTheatre(
                theatreId,
                request
        );
    }

    // Admin - deactivate
    @DeleteMapping("/{theatreId}")
    public DeleteTheatreResponse deleteTheatre(
            @PathVariable Long theatreId) {

        return theatreService.deleteTheatre(theatreId);
    }

    // Admin - activate
    @PatchMapping("/{theatreId}/activate")
    public TheatreStatusResponse activateTheatre(
            @PathVariable Long theatreId) {

        return theatreService.activateTheatre(theatreId);
    }
}
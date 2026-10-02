package com.moviebooking.controller;

import com.moviebooking.dto.DeleteShowResponse;
import com.moviebooking.dto.ShowRequest;
import com.moviebooking.dto.ShowResponse;
import com.moviebooking.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

        import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public ShowResponse createShow(
            @Valid @RequestBody ShowRequest request) {

        return showService.createShow(request);
    }

    @GetMapping("/admin")
    public List<ShowResponse> getAllShows() {
        return showService.getAllShows();
    }

    @GetMapping("/{showId}")
    public ShowResponse getShowById(@PathVariable Long showId) {
        return showService.getShowById(showId);
    }

    @GetMapping
    public List<ShowResponse> getShows(
            @RequestParam Long movieId,
            @RequestParam String city,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return showService.getShows(movieId, city, date);
    }

    @DeleteMapping("/{showId}")
    public DeleteShowResponse deleteShow(
            @PathVariable Long showId) {

        return showService.deleteShow(showId);
    }
}
package com.moviebooking.controller;

import com.moviebooking.dto.DeleteMovieResponse;
import com.moviebooking.dto.MovieRequest;
import com.moviebooking.dto.MovieResponse;
import com.moviebooking.dto.MovieStatusResponse;
import com.moviebooking.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("")
    public MovieResponse uploadMovies(@Valid @RequestBody MovieRequest movieRequest) {
        return movieService.uploadMovie(movieRequest);
    }

    @GetMapping("/search/{searchKey}")
    public Page<MovieResponse> searchMovies(
            @PathVariable String searchKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return movieService.getActiveMoviesBySearchKey(searchKey, page, size);
    }

    @GetMapping
    public Page<MovieResponse> getActiveMovies(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return movieService.getActiveMovies(city, page, size);
    }

    @GetMapping("/admin")
    public Page<MovieResponse> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return movieService.getMovies(page, size);
    }

    @GetMapping("/{movieId}")
    public MovieResponse getMovieById(@PathVariable long movieId) {
        return movieService.getMovieById(movieId);
    }

    @PutMapping("/{movieId}")
    public MovieResponse updateMovie(
            @PathVariable Long movieId,
            @Valid @RequestBody MovieRequest movieRequest) {

        return movieService.updateMovie(movieId, movieRequest);
    }

    @DeleteMapping("/{movieId}")
    public DeleteMovieResponse deleteMovie(@PathVariable long movieId) {
        return movieService.deleteMovie(movieId);
    }

    @PatchMapping("/{movieId}/activate")
    public MovieStatusResponse activateMovie(
            @PathVariable Long movieId) {

        return movieService.activateMovie(movieId);
    }
}

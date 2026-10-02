package com.moviebooking.service;

import com.moviebooking.dto.DeleteMovieResponse;
import com.moviebooking.dto.MovieRequest;
import com.moviebooking.dto.MovieResponse;
import com.moviebooking.dto.MovieStatusResponse;
import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Movie;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.enums.BookingStatus;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.enums.MovieStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.BookingRepo;
import com.moviebooking.repo.MovieRepo;
import com.moviebooking.repo.ShowRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;
    private final ShowRepo showRepo;
    private final BookingRepo bookingRepo;

    MovieService(MovieRepo movieRepo, ShowRepo showRepo, BookingRepo bookingRepo) {
        this.showRepo = showRepo;
        this.movieRepo = movieRepo;
        this.bookingRepo = bookingRepo;
    }

    public MovieResponse uploadMovie(MovieRequest movieRequest) {

        Movie movie = new Movie();

        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setReleaseDate(movieRequest.getReleaseDate());
        movie.setGenres(movieRequest.getGenres());

        Movie savedMovie = movieRepo.save(movie);

        MovieResponse movieResponse = new MovieResponse();

        movieResponse.setId(savedMovie.getId());
        movieResponse.setTitle(savedMovie.getTitle());
        movieResponse.setDescription(savedMovie.getDescription());
        movieResponse.setDurationMinutes(savedMovie.getDurationMinutes());
        movieResponse.setLanguage(savedMovie.getLanguage());
        movieResponse.setReleaseDate(savedMovie.getReleaseDate());
        movieResponse.setGenres(savedMovie.getGenres());

        return movieResponse;
    }

    public Page<MovieResponse> getMovies(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Movie> movies = movieRepo.findAll(pageable);

        return movies.map(this::mapToMovieResponse);
    }

    public Page<MovieResponse> getActiveMovies(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Movie> movies = movieRepo.findByMovieStatus(MovieStatus.ACTIVE, pageable);

        return movies.map(this::mapToMovieResponse);
    }

    private MovieResponse mapToMovieResponse(Movie movie) {

        MovieResponse response = new MovieResponse();

        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setLanguage(movie.getLanguage());
        response.setReleaseDate(movie.getReleaseDate());
        response.setGenres(new HashSet<>(movie.getGenres()));
        response.setMovieStatus(movie.getMovieStatus());
        return response;
    }

    public MovieResponse getMovieById(Long movieId) {

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new ApplicationException(
                        "Movie not found with id: " + movieId,
                        HttpStatus.NOT_FOUND
                ));

        return mapToMovieResponse(movie);
    }

    public MovieResponse updateMovie(Long movieId, MovieRequest movieRequest) {

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new ApplicationException(
                        "Movie not found with id: " + movieId,
                        HttpStatus.NOT_FOUND
                ));

        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setReleaseDate(movieRequest.getReleaseDate());
        movie.setGenres(new HashSet<>(movieRequest.getGenres()));

        Movie updatedMovie = movieRepo.save(movie);

        return mapToMovieResponse(updatedMovie);
    }

    @Transactional
    public DeleteMovieResponse deleteMovie(Long movieId) {

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new ApplicationException(
                        "Movie not found with id: " + movieId,
                        HttpStatus.NOT_FOUND
                ));

        if (movie.getMovieStatus() == MovieStatus.INACTIVE) {
            throw new ApplicationException(
                    "Movie has already been deleted",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDateTime now = LocalDateTime.now();

        List<Show> futureShows =
                showRepo.findByMovieIdAndStartTimeGreaterThanEqualAndShowStatus(
                        movieId,
                        now,
                        ShowStatus.ACTIVE
                );

        for (Show show : futureShows) {
            show.setShowStatus(ShowStatus.INACTIVE);
        }

        List<Booking> futureShowBookings =
                bookingRepo.findByShowIn(futureShows);

        for (Booking booking : futureShowBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        movie.setMovieStatus(MovieStatus.INACTIVE);

        return new DeleteMovieResponse(
                movie.getId(),
                movie.getMovieStatus(),
                futureShows.size(),
                futureShowBookings.size(),
                "Movie deleted successfully"
        );
    }

    @Transactional
    public MovieStatusResponse activateMovie(Long movieId) {

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new ApplicationException(
                        "Movie not found", HttpStatus.NOT_FOUND
                        ));

        if (movie.getMovieStatus() == MovieStatus.ACTIVE) {
            throw new ApplicationException(
                    "Movie is already active", HttpStatus.BAD_REQUEST
            );
        }

        movie.setMovieStatus(MovieStatus.ACTIVE);

        movieRepo.save(movie);

        return new MovieStatusResponse(
                movie.getId(),
                "ACTIVE",
                "Movie activated successfully"
        );
    }

    public Page<MovieResponse> getActiveMoviesBySearchKey(
            String searchKey,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Movie> movies =
                movieRepo.findByMovieStatusAndTitleStartsWith(
                        MovieStatus.ACTIVE,
                        searchKey,
                        pageable
                );

        return movies.map(this::mapToMovieResponse);
    }

    public Page<MovieResponse> getActiveMovies(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies = movieRepo.findActiveMoviesByCity(MovieStatus.ACTIVE, city, ShowStatus.ACTIVE, pageable);
        return movies.map(this::mapToMovieResponse);
    }
}

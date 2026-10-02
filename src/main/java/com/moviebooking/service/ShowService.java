package com.moviebooking.service;

import com.moviebooking.dto.DeleteShowResponse;
import com.moviebooking.dto.ShowRequest;
import com.moviebooking.dto.ShowResponse;
import com.moviebooking.entity.*;
import com.moviebooking.entity.enums.*;
import com.moviebooking.enums.MovieStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowService {
    private final MovieRepo movieRepo;
    private final ScreenRepo screenRepo;
    private final ShowRepo showRepo;
    private final BookingRepo bookingRepo;
    private final SeatRepo seatRepo;
    private final ShowSeatRepo showSeatRepo;

    ShowService(MovieRepo movieRepo, ScreenRepo screenRepo, ShowRepo showRepo, BookingRepo bookingRepo, SeatRepo seatRepo, ShowSeatRepo showSeatRepo) {
        this.movieRepo = movieRepo;
        this.screenRepo = screenRepo;
        this.showRepo = showRepo;
        this.bookingRepo = bookingRepo;
        this.seatRepo = seatRepo;
        this.showSeatRepo = showSeatRepo;
    }

    public List<ShowResponse> getAllShows() {

        return showRepo.findAll()
                .stream()
                .map(this::mapToShowResponse)
                .toList();
    }

    @Transactional
    public ShowResponse createShow(ShowRequest showRequest) {

        Movie movie = movieRepo.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ApplicationException(
                        "Movie not found",
                        HttpStatus.NOT_FOUND
                ));

        if (movie.getMovieStatus() == MovieStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot create show for an inactive movie",
                    HttpStatus.BAD_REQUEST
            );
        }

        Screen screen = screenRepo.findById(showRequest.getScreenId())
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found",
                        HttpStatus.NOT_FOUND
                ));

        if (screen.getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot create show for an inactive screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDateTime startTime = showRequest.getStartTime();

        LocalDateTime endTime =
                startTime.plusMinutes(movie.getDurationMinutes());

        boolean showExists =
                showRepo.existsByScreenAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        screen,
                        endTime,
                        startTime
                );

        if (showExists) {
            throw new ApplicationException(
                    "Time conflict with an existing show",
                    HttpStatus.CONFLICT
            );
        }

        Show show = new Show();

        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(startTime);
        show.setEndTime(endTime);

        Show savedShow = showRepo.save(show);

        List<Seat> seats = seatRepo.findByScreen(screen);

        List<ShowSeat> showSeats = seats.stream()
                .map(seat -> {
                    ShowSeat showSeat = new ShowSeat();

                    showSeat.setShow(savedShow);
                    showSeat.setSeat(seat);
                    showSeat.setStatus(ShowSeatStatus.AVAILABLE);
                    if (seat.getSeatType() == SeatType.REGULAR)
                        showSeat.setPrice(200);
                    else if (seat.getSeatType() == SeatType.PREMIUM)
                        showSeat.setPrice(400);
                    else if (seat.getSeatType() == SeatType.RECLINER)
                        showSeat.setPrice(800);
                    return showSeat;
                })
                .toList();

        showSeatRepo.saveAll(showSeats);

        return new ShowResponse(
                savedShow.getId(),
                savedShow.getMovie().getId(),
                savedShow.getScreen().getId(),
                savedShow.getScreen().getName(),
                savedShow.getScreen().getTheatre().getName(),
                savedShow.getScreen().getTheatre().getCity(),
                savedShow.getStartTime(),
                savedShow.getEndTime(),
                savedShow.getShowStatus()
        );
    }

    public ShowResponse getShowById(Long showId) {

        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new ApplicationException(
                        "Show not found",
                        HttpStatus.NOT_FOUND
                ));

        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getTheatre().getCity(),
                show.getStartTime(),
                show.getEndTime(),
                show.getShowStatus()
        );
    }

    public List<ShowResponse> getShows(
            Long movieId,
            String city,
            LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return showRepo
                .findByMovieIdAndScreenTheatreCityAndShowStatusAndStartTimeBetween(
                        movieId,
                        city,
                        ShowStatus.ACTIVE,
                        startOfDay,
                        endOfDay
                )
                .stream()
                .map(this::mapToShowResponse)
                .toList();
    }

    private ShowResponse mapToShowResponse(Show show) {

        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTheatre().getName(),
                show.getScreen().getTheatre().getCity(),
                show.getStartTime(),
                show.getEndTime(),
                show.getShowStatus()
        );
    }

    @Transactional
    public DeleteShowResponse deleteShow(Long showId) {

        Show show = showRepo.findById(showId)
                .orElseThrow(() -> new ApplicationException(
                        "Show not found",
                        HttpStatus.NOT_FOUND
                ));

        if (show.getShowStatus() == ShowStatus.INACTIVE) {
            throw new ApplicationException(
                    "Show is already inactive",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<Booking> bookings = bookingRepo.findByShow(show);

        int cancelledBookings = 0;

        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                cancelledBookings++;
            }
        }

        show.setShowStatus(ShowStatus.INACTIVE);

        return new DeleteShowResponse(
                show.getId(),
                show.getShowStatus(),
                show.getStartTime(),
                show.getEndTime(),
                cancelledBookings,
                "Show cancelled successfully"
        );
    }
}

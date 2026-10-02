package com.moviebooking.service;

import com.moviebooking.dto.DeleteTheatreResponse;
import com.moviebooking.dto.TheatreRequest;
import com.moviebooking.dto.TheatreResponse;
import com.moviebooking.dto.TheatreStatusResponse;
import com.moviebooking.entity.Booking;
import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.Theatre;
import com.moviebooking.entity.enums.BookingStatus;
import com.moviebooking.entity.enums.ScreenStatus;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.entity.enums.TheatreStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.BookingRepo;
import com.moviebooking.repo.ScreenRepo;
import com.moviebooking.repo.ShowRepo;
import com.moviebooking.repo.TheatreRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepo theatreRepo;
    private final ScreenRepo screenRepo;
    private final ShowRepo showRepo;
    private final BookingRepo bookingRepo;

    public TheatreService(
            TheatreRepo theatreRepo,
            ScreenRepo screenRepo,
            ShowRepo showRepo,
            BookingRepo bookingRepo) {

        this.theatreRepo = theatreRepo;
        this.screenRepo = screenRepo;
        this.showRepo = showRepo;
        this.bookingRepo = bookingRepo;
    }

    // =========================
    // CREATE THEATRE
    // =========================

    public TheatreResponse createTheatre(TheatreRequest request) {

        Theatre theatre = new Theatre();

        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setCity(request.getCity());

        Theatre savedTheatre = theatreRepo.save(theatre);

        return mapToTheatreResponse(savedTheatre);
    }

    // =========================
    // GET ACTIVE THEATRES
    // =========================

    public List<TheatreResponse> getTheatres(
            String city,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Theatre> theatres;

        if (city == null || city.isBlank()) {

            theatres =
                    theatreRepo.findByTheatreStatus(
                            TheatreStatus.ACTIVE,
                            pageable
                    );

        } else {

            theatres =
                    theatreRepo.findByCityIgnoreCaseAndTheatreStatus(
                            city,
                            TheatreStatus.ACTIVE,
                            pageable
                    );
        }

        return theatres.getContent()
                .stream()
                .map(this::mapToTheatreResponse)
                .toList();
    }

    // =========================
    // GET ALL THEATRES - ADMIN
    // =========================

    public List<TheatreResponse> getAllTheatres(
            String city,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Theatre> theatres;

        if (city == null || city.isBlank()) {

            theatres = theatreRepo.findAll(pageable);

        } else {

            theatres =
                    theatreRepo.findByCityIgnoreCase(
                            city,
                            pageable
                    );
        }

        return theatres.getContent()
                .stream()
                .map(this::mapToTheatreResponse)
                .toList();
    }

    // =========================
    // GET THEATRE BY ID
    // =========================

    public TheatreResponse getTheatreById(Long theatreId) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        return mapToTheatreResponse(theatre);
    }

    // =========================
    // UPDATE THEATRE
    // =========================

    @Transactional
    public TheatreResponse updateTheatre(
            Long theatreId,
            TheatreRequest request) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setCity(request.getCity());

        return mapToTheatreResponse(theatre);
    }

    // =========================
    // DEACTIVATE THEATRE
    // =========================

    @Transactional
    public DeleteTheatreResponse deleteTheatre(Long theatreId) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        if (theatre.getTheatreStatus() == TheatreStatus.INACTIVE) {

            throw new ApplicationException(
                    "Theatre has already been deactivated",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDateTime currentTime = LocalDateTime.now();

        List<Screen> theatreScreens =
                screenRepo.findByTheatreId(theatreId);

        List<Show> futureShows =
                showRepo.findByScreenInAndStartTimeGreaterThanEqualAndShowStatus(
                        theatreScreens,
                        currentTime,
                        ShowStatus.ACTIVE
                );

        List<Booking> futureBookings =
                bookingRepo.findByShowIn(futureShows);

        // Cancel future bookings
        for (Booking booking : futureBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        // Deactivate future shows
        for (Show show : futureShows) {
            show.setShowStatus(ShowStatus.INACTIVE);
        }

        // Deactivate screens
        for (Screen screen : theatreScreens) {
            screen.setScreenStatus(ScreenStatus.INACTIVE);
        }

        // Deactivate theatre
        theatre.setTheatreStatus(TheatreStatus.INACTIVE);

        return new DeleteTheatreResponse(
                theatre.getId(),
                theatre.getTheatreStatus(),
                theatreScreens.size(),
                futureShows.size(),
                futureBookings.size(),
                "Theatre deactivated successfully"
        );
    }

    // =========================
    // ACTIVATE THEATRE
    // =========================

    @Transactional
    public TheatreStatusResponse activateTheatre(Long theatreId) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        if (theatre.getTheatreStatus() == TheatreStatus.ACTIVE) {

            throw new ApplicationException(
                    "Theatre is already active",
                    HttpStatus.BAD_REQUEST
            );
        }

        theatre.setTheatreStatus(TheatreStatus.ACTIVE);

        return new TheatreStatusResponse(
                theatre.getId(),
                theatre.getTheatreStatus(),
                "Theatre activated successfully"
        );
    }

    // =========================
    // MAPPER
    // =========================

    private TheatreResponse mapToTheatreResponse(
            Theatre theatre) {

        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getAddress(),
                theatre.getCity(),
                theatre.getTheatreStatus(),
                theatre.getCreatedAt(),
                theatre.getUpdatedAt()
        );
    }
}
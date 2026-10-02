package com.moviebooking.service;

import com.moviebooking.dto.*;
import com.moviebooking.entity.*;
import com.moviebooking.entity.enums.BookingStatus;
import com.moviebooking.entity.enums.ScreenStatus;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.entity.enums.TheatreStatus;
import com.moviebooking.enums.MovieStatus;
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
public class ScreenService {

    private final TheatreRepo theatreRepo;
    private final ScreenRepo screenRepo;
    private final ShowRepo showRepo;
    private final BookingRepo bookingRepo;

    ScreenService(TheatreRepo theatreRepo, ScreenRepo screenRepo, ShowRepo showRepo, BookingRepo bookingRepo) {
        this.theatreRepo = theatreRepo;
        this.screenRepo = screenRepo;
        this.showRepo = showRepo;
        this.bookingRepo = bookingRepo;
    }

    public ScreenResponse createScreen(
            Long theatreId,
            ScreenRequest request) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        if (theatre.getTheatreStatus() == TheatreStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot create screen for an inactive theatre",
                    HttpStatus.BAD_REQUEST
            );
        }

        Screen screen = new Screen();

        screen.setName(request.getName());
        screen.setTheatre(theatre);

        Screen savedScreen = screenRepo.save(screen);

        return new ScreenResponse(
                savedScreen.getId(),
                savedScreen.getName(),
                savedScreen.getTheatre().getId(),
                savedScreen.getScreenStatus(),
                savedScreen.getTheatre().getName(),
                savedScreen.getCreatedAt()
        );
    }

    public List<ScreenResponse> getScreensByTheatre(Long theatreId) {

        Theatre theatre = theatreRepo.findById(theatreId)
                .orElseThrow(() -> new ApplicationException(
                        "Theatre not found with id: " + theatreId,
                        HttpStatus.NOT_FOUND
                ));

        return screenRepo.findByTheatreId(theatreId)
                .stream()
                .map(this::mapToScreenResponse)
                .toList();
    }

    private ScreenResponse mapToScreenResponse(Screen screen) {

        return new ScreenResponse(
                screen.getId(),
                screen.getName(),
                screen.getTheatre().getId(),
                screen.getScreenStatus(),
                screen.getTheatre().getName(),
                screen.getCreatedAt()
        );
    }

    public ScreenResponse getScreenById(Long screenId) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        return mapToScreenResponse(screen);
    }

    @Transactional
    public ScreenResponse updateScreen(
            Long screenId,
            ScreenRequest request) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        if (screen.getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot update an inactive screen",
                    HttpStatus.BAD_REQUEST
            );
        }

        screen.setName(request.getName());

        return mapToScreenResponse(screen);
    }

    @Transactional
    public DeleteScreenResponse deleteScreen(Long screenId) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        if (screen.getScreenStatus() == ScreenStatus.INACTIVE) {
            throw new ApplicationException(
                    "Screen has already been deleted",
                    HttpStatus.BAD_REQUEST
            );
        }

        LocalDateTime currentTime = LocalDateTime.now();

        List<Show> futureShows =
                showRepo.findByScreenAndStartTimeGreaterThanEqualAndShowStatus(
                        screen,
                        currentTime,
                        ShowStatus.ACTIVE
                );

        List<Booking> futureBookings =
                bookingRepo.findByShowIn(futureShows);

        for (Booking booking : futureBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        for (Show show : futureShows) {
            show.setShowStatus(ShowStatus.INACTIVE);
        }

        screen.setScreenStatus(ScreenStatus.INACTIVE);

        return new DeleteScreenResponse(
                screen.getId(),
                screen.getScreenStatus(),
                futureShows.size(),
                futureBookings.size(),
                "Screen deleted successfully"
        );
    }

    @Transactional
    public ScreenStatusResponse activateScreen(Long screenId) {

        Screen screen = screenRepo.findById(screenId)
                .orElseThrow(() -> new ApplicationException(
                        "Screen not found with id: " + screenId,
                        HttpStatus.NOT_FOUND
                ));

        if (screen.getScreenStatus() == ScreenStatus.ACTIVE) {
            throw new ApplicationException(
                    "Screen is already active",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (screen.getTheatre().getTheatreStatus() == TheatreStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot activate a screen belonging to an inactive theatre",
                    HttpStatus.BAD_REQUEST
            );
        }

        screen.setScreenStatus(ScreenStatus.ACTIVE);

        return new ScreenStatusResponse(
                screen.getId(),
                screen.getScreenStatus(),
                "Screen activated successfully"
        );
    }

    public Page<ScreenResponse> getActiveScreensBySearchKey(String searchKey, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Screen> screens =
                screenRepo.findByScreenStatusAndNameStartsWith(
                        ScreenStatus.ACTIVE,
                        searchKey,
                        pageable
                );

        return screens.map(this::mapToScreenResponse);
    }
}

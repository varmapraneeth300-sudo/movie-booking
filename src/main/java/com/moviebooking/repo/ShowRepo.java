package com.moviebooking.repo;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Show;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepo extends JpaRepository<Show, Long> {
    List<Show> findByMovieIdAndStartTimeGreaterThanEqualAndShowStatus(Long movieId, LocalDateTime currentTime, ShowStatus showStatus);

    List<Show> findByScreenInAndStartTimeGreaterThanEqualAndShowStatus(
            List<Screen> screens,
            LocalDateTime startTime,
            ShowStatus showStatus
    );

    List<Show> findByScreenAndStartTimeGreaterThanEqualAndShowStatus(
            Screen screen,
            LocalDateTime startTime,
            ShowStatus showStatus
    );

    List<Show> findByMovieIdAndScreenTheatreCityAndStartTimeBetween(
            Long movieId,
            String city,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Show> findByMovieIdAndScreenTheatreCityAndShowStatusAndStartTimeBetween(
            Long movieId,
            String city,
            ShowStatus showStatus,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    boolean existsByScreenAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Screen screen, LocalDateTime endTime, LocalDateTime startTime);

}


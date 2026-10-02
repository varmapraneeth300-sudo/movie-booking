package com.moviebooking.repo;

import com.moviebooking.entity.Movie;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.enums.MovieStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  MovieRepo extends JpaRepository<Movie, Long> {
    Page<Movie> findAll(@NonNull Pageable pageable);

    Page<Movie> findByMovieStatus(MovieStatus movieStatus, Pageable pageable);

    Page<Movie> findByMovieStatusAndTitleStartsWith(MovieStatus movieStatus, String searchKey, Pageable pageable);

    @Query("""
    SELECT DISTINCT s.movie
    FROM Show s
    WHERE s.movie.movieStatus = :movieStatus
      AND s.screen.theatre.city = :city
      AND s.showStatus = :showStatus
""")
    Page<Movie> findActiveMoviesByCity(
            @Param("movieStatus") MovieStatus movieStatus,
            @Param("city") String city,
            @Param("showStatus") ShowStatus showStatus,
            Pageable pageable
    );
}

package com.moviebooking.repo;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.enums.ScreenStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepo extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatreId(long theatreId);

    Page<Screen> findByScreenStatusAndNameStartsWith(ScreenStatus screenStatus, String searchKey, Pageable pageable);
}

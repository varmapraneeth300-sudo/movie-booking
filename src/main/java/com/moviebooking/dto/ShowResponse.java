package com.moviebooking.dto;

import com.moviebooking.entity.enums.ShowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ShowResponse {
    private Long id;
    private Long movieId;
    private Long screenId;
    private String screenName;
    private String theatreName;
    private String theatreCity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ShowStatus showStatus;
}
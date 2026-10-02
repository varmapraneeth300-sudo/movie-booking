package com.moviebooking.dto;

import com.moviebooking.entity.enums.ScreenStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ScreenResponse {

    private Long id;
    private String name;
    private Long theatreId;
    private ScreenStatus screenStatus;
    private String theatreName;
    private LocalDateTime createdAt;
}
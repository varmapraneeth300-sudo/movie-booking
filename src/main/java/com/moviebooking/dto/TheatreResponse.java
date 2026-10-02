package com.moviebooking.dto;

import com.moviebooking.entity.enums.TheatreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TheatreResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private TheatreStatus theatreStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
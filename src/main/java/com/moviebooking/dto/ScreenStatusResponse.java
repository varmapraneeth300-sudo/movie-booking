package com.moviebooking.dto;

import com.moviebooking.entity.enums.ScreenStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScreenStatusResponse {

    private Long screenId;
    private ScreenStatus screenStatus;
    private String message;
}
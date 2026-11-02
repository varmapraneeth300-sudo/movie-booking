package com.moviebooking.service;

import lombok.Data;

@Data
public class ReleaseSeatRequestDto {
    private Long showId;
    private Long seatId;
}

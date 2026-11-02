package com.moviebooking.dto;

import lombok.Data;

@Data
public class LockSeatRequestDto {
    private long seatId;
    private long showId;
}

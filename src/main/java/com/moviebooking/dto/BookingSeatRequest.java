package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingSeatRequest {

    @NotNull(message = "Seat ID is required")
    private Long seatId;
}

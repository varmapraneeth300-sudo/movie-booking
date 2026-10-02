package com.moviebooking.dto;

import com.moviebooking.entity.enums.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatRequest {

    @NotBlank(message = "Row label is required")
    @Size(max = 10, message = "Row label must not exceed 10 characters")
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be greater than 0")
    private Integer seatNumber;

    @NotNull(message = "Seat type is required")
    private SeatType seatType;
}
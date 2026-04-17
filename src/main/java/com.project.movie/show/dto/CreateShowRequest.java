package com.project.movie.show.dto;


import com.project.movie.common.enums.SeatType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Value
public class CreateShowRequest {

    @NotNull(message = "Theatre ID is required")
    UUID theatreId;

    @NotNull(message = "Movie ID is required")
    UUID movieId;

    @NotBlank(message = "Language is required")
    @Size(max = 100)
    String language;

    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date must be today or in the future")
    LocalDate showDate;

    @NotNull(message = "Start time is required")
    LocalTime startTime;

    @NotNull(message = "Seats are required")
    @Size(min = 1, message = "At least one seat must be defined")
    @Valid
    List<SeatAllocationRequest> seats;

    @Value
    public static class SeatAllocationRequest {

        @NotBlank(message = "Row label is required")
        @Size(max = 5)
        String rowLabel;

        @Min(value = 1, message = "Seat number must be positive")
        int seatNumber;

        @NotNull(message = "Seat type is required")
        SeatType seatType;
    }
}

package com.project.movie.show.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

@Value
public class UpdateShowRequest {

    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date must be today or in the future")
    LocalDate showDate;

    @NotNull(message = "Start time is required")
    LocalTime startTime;
}

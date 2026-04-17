package com.project.movie.show.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class ShowResponse {

    UUID id;
    UUID theatreId;
    String theatreName;
    UUID movieId;
    String movieTitle;
    String language;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate showDate;

    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime;

    int totalSeats;
    int availableSeats;
    String status;
}

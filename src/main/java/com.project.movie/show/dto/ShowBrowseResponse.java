package com.project.movie.show.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class ShowBrowseResponse {

    UUID movieId;
    String movieTitle;
    String genre;
    int durationMinutes;
    LocalDate showDate;

    List<TheatreShowGroup> theatres;

    @Value
    @Builder
    public static class TheatreShowGroup {
        UUID theatreId;
        String theatreName;
        String theatreAddress;
        String cityName;
        List<ShowSummary> shows;
    }

    @Value
    @Builder
    public static class ShowSummary {
        UUID showId;
        String language;

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime;

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime;

        int availableSeats;
        int totalSeats;
        String status;
    }
}

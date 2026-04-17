package com.project.movie.screenmovies.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class MovieResponse {
    UUID id;
    String title;
    String genre;
    int durationMinutes;
    String description;
}

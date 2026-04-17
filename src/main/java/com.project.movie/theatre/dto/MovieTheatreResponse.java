package com.project.movie.theatre.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class MovieTheatreResponse {
    UUID id;
    String name;
    String address;
    UUID cityId;
    String cityName;
}

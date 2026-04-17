package com.project.movie.city.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CityResponse {
    UUID id;
    String name;
    String state;
    String country;
}

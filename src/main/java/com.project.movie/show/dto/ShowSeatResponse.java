package com.project.movie.show.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ShowSeatResponse {
    UUID id;
    String rowLabel;
    int seatNumber;
    String seatType;
    String status;
}

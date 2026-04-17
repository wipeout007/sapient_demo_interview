package com.project.movie.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class BookingResponse {

    UUID bookingId;
    String status;

    UUID showId;
    String movieTitle;
    String theatreName;
    String language;
    LocalDate showDate;
    LocalTime startTime;

    String customerName;
    String customerEmail;
    String customerPhone;

    int totalSeats;
    BigDecimal totalAmount;
    Instant bookedAt;

    List<BookingItemResponse> seats;

    @Value
    @Builder
    public static class BookingItemResponse {
        UUID showSeatId;
        String rowLabel;
        int seatNumber;
        String seatType;
        BigDecimal price;
    }
}

package com.project.movie.booking.controller;


import com.project.movie.booking.dto.BookingResponse;
import com.project.movie.booking.dto.CreateBookingRequest;
import com.project.movie.booking.service.BookingService;
import com.project.movie.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> book(
            @Valid @RequestBody CreateBookingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bookingService.book(request)));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable UUID bookingId) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBooking(bookingId)));
    }
}

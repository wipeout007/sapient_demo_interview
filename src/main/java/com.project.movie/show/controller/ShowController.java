package com.project.movie.show.controller;


import com.project.movie.common.response.ApiResponse;
import com.project.movie.show.dto.CreateShowRequest;
import com.project.movie.show.dto.ShowBrowseResponse;
import com.project.movie.show.dto.ShowResponse;
import com.project.movie.show.dto.ShowSeatResponse;
import com.project.movie.show.dto.UpdateShowRequest;
import com.project.movie.show.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/browse")
    public ResponseEntity<ApiResponse<ShowBrowseResponse>> browse(
            @RequestParam UUID movieId,
            @RequestParam UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(showService.browseShows(movieId, cityId, date)));
    }


    @GetMapping("/{showId}/seats")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getAvailableSeats(
            @PathVariable UUID showId) {

        return ResponseEntity.ok(ApiResponse.ok(showService.getAvailableSeats(showId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @Valid @RequestBody CreateShowRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(showService.createShow(request)));
    }

    @PutMapping("/{showId}")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShow(
            @PathVariable UUID showId,
            @Valid @RequestBody UpdateShowRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(showService.updateShow(showId, request)));
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<ApiResponse<Void>> deleteShow(@PathVariable UUID showId) {
        showService.deleteShow(showId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

package com.project.movie.theatre.controller;


import com.project.movie.common.response.ApiResponse;
import com.project.movie.theatre.dto.MovieTheatreResponse;
import com.project.movie.theatre.service.MovieTheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final MovieTheatreService theatreService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieTheatreResponse>>> getTheatres(
            @RequestParam UUID cityId) {
        return ResponseEntity.ok(ApiResponse.ok(theatreService.getTheatresByCity(cityId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieTheatreResponse>> getTheatre(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(theatreService.getTheatre(id)));
    }
}

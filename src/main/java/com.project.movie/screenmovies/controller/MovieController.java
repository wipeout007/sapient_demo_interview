package com.project.movie.screenmovies.controller;


import com.project.movie.common.response.ApiResponse;
import com.project.movie.screenmovies.dto.MovieResponse;
import com.project.movie.screenmovies.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getAllActiveMovies()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovie(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(movieService.getMovie(id)));
    }
}

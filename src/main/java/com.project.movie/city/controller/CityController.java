package com.project.movie.city.controller;


import com.project.movie.city.dto.CityResponse;
import com.project.movie.city.service.CityService;
import com.project.movie.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        return ResponseEntity.ok(ApiResponse.ok(cityService.getAllActiveCities()));
    }
}

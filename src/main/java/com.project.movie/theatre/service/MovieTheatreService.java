package com.project.movie.theatre.service;


import com.project.movie.common.exception.ResourceNotFoundException;
import com.project.movie.theatre.dto.MovieTheatreResponse;
import com.project.movie.theatre.entity.MovieTheatre;
import com.project.movie.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieTheatreService {

    private final TheatreRepository theatreRepository;

    @Transactional(readOnly = true)
    public List<MovieTheatreResponse> getTheatresByCity(UUID cityId) {
        return theatreRepository.findAllByCityIdAndActiveTrue(cityId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieTheatreResponse getTheatre(UUID id) {
        return theatreRepository.findActiveById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + id));
    }

    private MovieTheatreResponse toResponse(MovieTheatre t) {
        return MovieTheatreResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .address(t.getAddress())
                .cityId(t.getCity().getId())
                .cityName(t.getCity().getName())
                .build();
    }
}

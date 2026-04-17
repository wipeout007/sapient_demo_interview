package com.project.movie.screenmovies.service;


import com.project.movie.common.exception.ResourceNotFoundException;
import com.project.movie.screenmovies.dto.MovieResponse;
import com.project.movie.screenmovies.entity.MovieDetails;
import com.project.movie.screenmovies.repository.MovieDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieDetailsRepository movieDetailsRepository;

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllActiveMovies() {
        return movieDetailsRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovie(UUID id) {
        return movieDetailsRepository.findById(id)
                .filter(m -> m.isActive())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
    }

    private MovieResponse toResponse(MovieDetails m) {
        return MovieResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .genre(m.getGenre())
                .durationMinutes(m.getDurationMinutes())
                .description(m.getDescription())
                .build();
    }
}

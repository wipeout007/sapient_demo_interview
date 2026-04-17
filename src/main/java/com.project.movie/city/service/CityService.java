package com.project.movie.city.service;


import com.project.movie.city.dto.CityResponse;
import com.project.movie.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<CityResponse> getAllActiveCities() {
        return cityRepository.findAllByActiveTrue().stream()
                .map(c -> CityResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .state(c.getState())
                        .country(c.getCountry())
                        .build())
                .collect(Collectors.toList());
    }
}

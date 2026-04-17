package com.project.movie.show.service;


import com.project.movie.common.enums.ShowStatus;
import com.project.movie.common.exception.BusinessException;
import com.project.movie.common.exception.ResourceNotFoundException;
import com.project.movie.screenmovies.entity.MovieDetails;
import com.project.movie.screenmovies.repository.MovieDetailsRepository;
import com.project.movie.show.dto.CreateShowRequest;
import com.project.movie.show.dto.ShowBrowseResponse;
import com.project.movie.show.dto.ShowResponse;
import com.project.movie.show.dto.ShowSeatResponse;
import com.project.movie.show.dto.UpdateShowRequest;
import com.project.movie.show.entity.MovieShows;
import com.project.movie.show.entity.MovieShowSeat;
import com.project.movie.show.repository.MovieShowsRepository;
import com.project.movie.show.repository.ShowSeatRepository;
import com.project.movie.theatre.entity.MovieTheatre;
import com.project.movie.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowService {

    private static final String CACHE_BROWSE = "show-browse";
    private static final int SHOW_BUFFER_MINUTES = 15;

    private final MovieShowsRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final TheatreRepository theatreRepository;
    private final MovieDetailsRepository movieDetailsRepository;

    @Cacheable(value = CACHE_BROWSE, key = "#movieId + '::' + #cityId + '::' + #showDate", unless = "#result.theatres.isEmpty()")
    @Transactional(readOnly = true)
    public ShowBrowseResponse browseShows(UUID movieId, UUID cityId, LocalDate showDate) {
        log.info("Browsing shows for movieId={} cityId={} date={}", movieId, cityId, showDate);

        List<MovieShows> movieShows = showRepository.findShowsForBrowse(movieId, cityId, showDate, ShowStatus.SCHEDULED);

        if (movieShows.isEmpty()) {
            return buildEmptyBrowseResponse(movieId, cityId, showDate);
        }

        MovieDetails movieDetails = movieShows.get(0).getMovieDetails();

        Map<UUID, List<MovieShows>> byTheatre = movieShows.stream()
                .collect(Collectors.groupingBy(s -> s.getTheatre().getId()));

        List<ShowBrowseResponse.TheatreShowGroup> theatreGroups = byTheatre.entrySet().stream()
                .map(entry -> {
                    MovieTheatre movieTheatre = entry.getValue().get(0).getTheatre();
                    List<ShowBrowseResponse.ShowSummary> summaries = entry.getValue().stream()
                            .map(this::toShowSummary)
                            .collect(Collectors.toList());
                    return ShowBrowseResponse.TheatreShowGroup.builder()
                            .theatreId(movieTheatre.getId())
                            .theatreName(movieTheatre.getName())
                            .theatreAddress(movieTheatre.getAddress())
                            .cityName(movieTheatre.getCity().getName())
                            .shows(summaries)
                            .build();
                })
                .collect(Collectors.toList());

        return ShowBrowseResponse.builder()
                .movieId(movieDetails.getId())
                .movieTitle(movieDetails.getTitle())
                .genre(movieDetails.getGenre())
                .durationMinutes(movieDetails.getDurationMinutes())
                .showDate(showDate)
                .theatres(theatreGroups)
                .build();
    }

    /**
     * WRITE SCENARIO: Theatre partner creates a show.
     * - Derives end_time from movie duration + buffer
     * - Validates no time slot overlap for the same theatre on the same date
     * - Bulk-creates show_seat rows for all allocated seats
     * - Evicts Redis browse cache for this movie/city/date combination
     */
    @CacheEvict(value = CACHE_BROWSE, allEntries = true)
    @Transactional
    public ShowResponse createShow(CreateShowRequest request) {
        MovieTheatre movieTheatre = theatreRepository.findActiveById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Theatre not found: " + request.getTheatreId()));

        MovieDetails movieDetails = movieDetailsRepository.findById(request.getMovieId())
                .filter(MovieDetails::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found: " + request.getMovieId()));

        LocalTime endTime = request.getStartTime()
                .plusMinutes(movieDetails.getDurationMinutes())
                .plusMinutes(SHOW_BUFFER_MINUTES);

        validateNoOverlap(movieTheatre.getId(), request.getShowDate(), request.getStartTime(), endTime);

        MovieShows movieShows = MovieShows.builder()
                .theatre(movieTheatre)
                .movieDetails(movieDetails)
                .language(request.getLanguage())
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .totalSeats(request.getSeats().size())
                .availableSeats(request.getSeats().size())
                .status(ShowStatus.SCHEDULED)
                .build();

        movieShows = showRepository.save(movieShows);

        // Bulk create show_seat rows
        final MovieShows savedMovieShows = movieShows;
        List<MovieShowSeat> seats = request.getSeats().stream()
                .map(s -> MovieShowSeat.builder()
                        .movieShows(savedMovieShows)
                        .rowLabel(s.getRowLabel())
                        .seatNumber(s.getSeatNumber())
                        .seatType(s.getSeatType())
                        .build())
                .collect(Collectors.toList());

        showSeatRepository.saveAll(seats);
        log.info("Created show id={} with {} seats", movieShows.getId(), seats.size());

        return toShowResponse(movieShows);
    }

    /**
     * WRITE SCENARIO: Theatre partner updates show date/time.
     * Only SCHEDULED shows can be updated.
     * Recalculates end_time and re-validates overlap.
     */
    @CacheEvict(value = CACHE_BROWSE, allEntries = true)
    @Transactional
    public ShowResponse updateShow(UUID showId, UpdateShowRequest request) {
        MovieShows movieShows = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (movieShows.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED shows can be updated.");
        }

        LocalTime endTime = request.getStartTime()
                .plusMinutes(movieShows.getMovieDetails().getDurationMinutes())
                .plusMinutes(SHOW_BUFFER_MINUTES);

        // Check overlap excluding self
        validateNoOverlapExcluding(movieShows.getTheatre().getId(),
                request.getShowDate(), request.getStartTime(), endTime, showId);

        movieShows.setShowDate(request.getShowDate());
        movieShows.setStartTime(request.getStartTime());
        movieShows.setEndTime(endTime);

        log.info("Updated show id={}", showId);
        return toShowResponse(showRepository.save(movieShows));
    }

    /**
     * WRITE SCENARIO: Theatre partner deletes (cancels) a show.
     * Soft-delete via status = CANCELLED.
     */
    @CacheEvict(value = CACHE_BROWSE, allEntries = true)
    @Transactional
    public void deleteShow(UUID showId) {
        MovieShows movieShows = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (movieShows.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException("Only SCHEDULED shows can be cancelled.");
        }

        movieShows.setStatus(ShowStatus.CANCELLED);
        showRepository.save(movieShows);
        log.info("Cancelled show id={}", showId);
    }

    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getAvailableSeats(UUID showId) {
        showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        return showSeatRepository
                .findAllByIdAndStatus(showId, com.project.movie.common.enums.SeatStatus.AVAILABLE)
                .stream()
                .map(this::toShowSeatResponse)
                .collect(Collectors.toList());
    }

    private void validateNoOverlap(UUID theatreId, LocalDate date, LocalTime start, LocalTime end) {
        List<MovieShows> existing = showRepository.findShowsOnDateForTheatre(theatreId, date);
        checkOverlap(existing, start, end, null);
    }

    private void validateNoOverlapExcluding(UUID theatreId, LocalDate date,
                                            LocalTime start, LocalTime end, UUID excludeShowId) {
        List<MovieShows> existing = showRepository.findShowsOnDateForTheatre(theatreId, date);
        checkOverlap(existing, start, end, excludeShowId);
    }

    private void checkOverlap(List<MovieShows> existing, LocalTime start, LocalTime end, UUID excludeId) {
        existing.stream()
                .filter(s -> excludeId == null || !s.getId().equals(excludeId))
                .filter(s -> s.getStatus() == ShowStatus.SCHEDULED)
                .forEach(s -> {
                    boolean overlaps = start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime());
                    if (overlaps) {
                        throw new BusinessException(
                                "Show time overlaps with existing show at " + s.getStartTime());
                    }
                });
    }

    private ShowBrowseResponse buildEmptyBrowseResponse(UUID movieId, UUID cityId, LocalDate showDate) {
        return showRepository.findById(movieId)
                .map(s -> ShowBrowseResponse.builder()
                        .movieId(movieId)
                        .showDate(showDate)
                        .theatres(List.of())
                        .build())
                .orElse(ShowBrowseResponse.builder()
                        .movieId(movieId)
                        .showDate(showDate)
                        .theatres(List.of())
                        .build());
    }

    private ShowBrowseResponse.ShowSummary toShowSummary(MovieShows movieShows) {
        return ShowBrowseResponse.ShowSummary.builder()
                .showId(movieShows.getId())
                .language(movieShows.getLanguage())
                .startTime(movieShows.getStartTime())
                .endTime(movieShows.getEndTime())
                .availableSeats(movieShows.getAvailableSeats())
                .totalSeats(movieShows.getTotalSeats())
                .status(movieShows.getStatus().name())
                .build();
    }

    private ShowResponse toShowResponse(MovieShows movieShows) {
        return ShowResponse.builder()
                .id(movieShows.getId())
                .theatreId(movieShows.getTheatre().getId())
                .theatreName(movieShows.getTheatre().getName())
                .movieId(movieShows.getMovieDetails().getId())
                .movieTitle(movieShows.getMovieDetails().getTitle())
                .language(movieShows.getLanguage())
                .showDate(movieShows.getShowDate())
                .startTime(movieShows.getStartTime())
                .endTime(movieShows.getEndTime())
                .totalSeats(movieShows.getTotalSeats())
                .availableSeats(movieShows.getAvailableSeats())
                .status(movieShows.getStatus().name())
                .build();
    }

    private ShowSeatResponse toShowSeatResponse(MovieShowSeat s) {
        return ShowSeatResponse.builder()
                .id(s.getId())
                .rowLabel(s.getRowLabel())
                .seatNumber(s.getSeatNumber())
                .seatType(s.getSeatType().name())
                .status(s.getStatus().name())
                .build();
    }
}

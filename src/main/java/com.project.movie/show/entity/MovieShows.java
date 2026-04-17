package com.project.movie.show.entity;


import com.project.movie.common.audit.Auditable;
import com.project.movie.common.enums.ShowStatus;
import com.project.movie.screenmovies.entity.MovieDetails;
import com.project.movie.theatre.entity.MovieTheatre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "movie_shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieShows extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theatre_id", nullable = false)
    private MovieTheatre theatre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private MovieDetails movieDetails;

    @Column(nullable = false, length = 100)
    private String language;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ShowStatus status = ShowStatus.SCHEDULED;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;
}

package com.project.movie.show.repository;


import com.project.movie.common.enums.ShowStatus;
import com.project.movie.show.entity.MovieShows;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieShowsRepository extends JpaRepository<MovieShows, UUID> {

    @Query("""
            SELECT s FROM MovieShows s JOIN FETCH s.theatre t JOIN FETCH t.city c JOIN FETCH s.movieDetails m WHERE m.id = :movieId 
                        AND c.id = :cityId AND s.showDate = :showDate AND s.status = :status AND t.active = true 
                                    AND m.active = true ORDER BY s.startTime ASC """)
    List<MovieShows> findShowsForBrowse(@Param("movieId") UUID movieId, @Param("cityId") UUID cityId, @Param("showDate") LocalDate showDate,
                                        @Param("status") ShowStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MovieShows s WHERE s.id = :id")
    Optional<MovieShows> findByIdWithLock(@Param("id") UUID id);


    @Query("""
            SELECT s FROM MovieShows s WHERE s.theatre.id = :theatreId AND s.showDate = :showDate AND s.status = 'SCHEDULED' """)
    List<MovieShows> findShowsOnDateForTheatre(@Param("theatreId") UUID theatreId, @Param("showDate") LocalDate showDate);
}

package com.project.movie.show.repository;


import com.project.movie.common.enums.SeatStatus;
import com.project.movie.show.entity.MovieShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShowSeatRepository extends JpaRepository<MovieShowSeat, UUID> {

    List<MovieShowSeat> findAllByIdAndStatus(UUID id, SeatStatus status);

    @Query(value = """
            SELECT * FROM movie_show_seat WHERE id IN (:seatIds) AND status = 'AVAILABLE' FOR UPDATE SKIP LOCKED """, nativeQuery = true)
    List<MovieShowSeat> findAvailableSeatsWithLock(@Param("seatIds") List<UUID> seatIds);

}

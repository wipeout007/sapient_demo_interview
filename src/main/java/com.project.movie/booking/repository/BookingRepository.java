package com.project.movie.booking.repository;


import com.project.movie.booking.entity.MovieBookingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<MovieBookingDetails, UUID> {

    @Query("""
            SELECT b FROM MovieBookingDetails b JOIN FETCH b.items i JOIN FETCH i.movieShowSeat ss WHERE b.id = :id
            """)
    Optional<MovieBookingDetails> findByIdWithItems(@Param("id") UUID id);
}

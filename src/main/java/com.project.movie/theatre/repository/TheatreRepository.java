package com.project.movie.theatre.repository;


import com.project.movie.theatre.entity.MovieTheatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TheatreRepository extends JpaRepository<MovieTheatre, UUID> {

    List<MovieTheatre> findAllByCityIdAndActiveTrue(UUID cityId);

    @Query("""
            SELECT mt FROM MovieTheatre mt JOIN FETCH mt.city c WHERE mt.id = :id AND mt.active = true
            """)
    Optional<MovieTheatre> findActiveById(@Param("id") UUID id);
}

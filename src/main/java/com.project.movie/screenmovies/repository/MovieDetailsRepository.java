package com.project.movie.screenmovies.repository;


import com.project.movie.screenmovies.entity.MovieDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieDetailsRepository extends JpaRepository<MovieDetails, UUID> {

    List<MovieDetails> findAllByActiveTrue();
}

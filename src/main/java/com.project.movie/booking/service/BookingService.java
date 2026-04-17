package com.project.movie.booking.service;


import com.project.movie.booking.dto.BookingResponse;
import com.project.movie.booking.dto.CreateBookingRequest;
import com.project.movie.booking.entity.MovieBookingDetails;
import com.project.movie.booking.entity.MovieBookingItem;
import com.project.movie.booking.repository.BookingRepository;
import com.project.movie.common.enums.BookingStatus;
import com.project.movie.common.enums.PaymentStatus;
import com.project.movie.common.enums.SeatStatus;
import com.project.movie.common.enums.ShowStatus;
import com.project.movie.common.exception.BusinessException;
import com.project.movie.common.exception.ResourceNotFoundException;
import com.project.movie.common.exception.SeatUnavailableException;
import com.project.movie.show.entity.MovieShows;
import com.project.movie.show.entity.MovieShowSeat;
import com.project.movie.show.repository.MovieShowsRepository;
import com.project.movie.show.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    // Fixed price per seat type — in a real system this would be on the show or a pricing table
    private static final BigDecimal REGULAR_PRICE   = new BigDecimal("200.00");
    private static final BigDecimal PREMIUM_PRICE   = new BigDecimal("350.00");
    private static final BigDecimal RECLINER_PRICE  = new BigDecimal("500.00");

    private final BookingRepository bookingRepository;
    private final MovieShowsRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional
    public BookingResponse book(CreateBookingRequest request) {
        log.info("Booking request: showId={} seats={} customer={}",
                request.getShowId(), request.getSeatIds(), request.getCustomerEmail());

        // Step 1: Lock the show row (pessimistic write) — prevents concurrent available_seats updates
        MovieShows movieShows = showRepository.findByIdWithLock(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Show not found: " + request.getShowId()));

        // Step 2: Validate show is bookable
        if (movieShows.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException("Show is not available for booking. Status: " + movieShows.getStatus());
        }

        int requestedCount = request.getSeatIds().size();

        if (movieShows.getAvailableSeats() < requestedCount) {
            throw new SeatUnavailableException(
                    "Not enough seats available. Requested: " + requestedCount +
                    ", Available: " + movieShows.getAvailableSeats());
        }

        // Any seat already locked by another concurrent transaction is excluded (SKIP LOCKED)
        List<MovieShowSeat> lockedSeats = showSeatRepository
                .findAvailableSeatsWithLock(request.getSeatIds());

        // Step 4: If we got fewer seats than requested, some were grabbed concurrently
        if (lockedSeats.size() != requestedCount) {
            List<UUID> lockedIds = lockedSeats.stream()
                    .map(MovieShowSeat::getId)
                    .collect(Collectors.toList());
            List<UUID> unavailable = request.getSeatIds().stream()
                    .filter(id -> !lockedIds.contains(id))
                    .collect(Collectors.toList());
            throw new SeatUnavailableException(
                    "The following seats are no longer available: " + unavailable);
        }


        lockedSeats.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
        showSeatRepository.saveAll(lockedSeats);

        movieShows.setAvailableSeats(movieShows.getAvailableSeats() - requestedCount);
        showRepository.save(movieShows);

        // Step 7: Build booking + items and persist in one shot
        BigDecimal totalAmount = lockedSeats.stream()
                .map(s -> priceFor(s))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MovieBookingDetails movieBookingDetails = MovieBookingDetails.builder()
                .movieShows(movieShows)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .totalSeats(requestedCount)
                .totalAmount(totalAmount)
                .paymentStatus(PaymentStatus.DONE)   //just for demo not involving payment service of payment method
                .status(BookingStatus.CONFIRMED)
                .build();

        List<MovieBookingItem> items = lockedSeats.stream()
                .map(seat -> MovieBookingItem.builder()
                        .movieBookingDetails(movieBookingDetails)
                        .movieShowSeat(seat)
                        .price(priceFor(seat))
                        .build())
                .collect(Collectors.toList());

        movieBookingDetails.getItems().addAll(items);
        MovieBookingDetails saved = bookingRepository.save(movieBookingDetails);

        log.info("Booking confirmed: id={} seats={} amount={}",
                saved.getId(), requestedCount, totalAmount);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID bookingId) {
        MovieBookingDetails movieBookingDetails = bookingRepository.findByIdWithItems(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        return toResponse(movieBookingDetails);
    }

    private BigDecimal priceFor(MovieShowSeat seat) {
        return switch (seat.getSeatType()) {
            case PREMIUM  -> PREMIUM_PRICE;
            case RECLINER -> RECLINER_PRICE;
            default       -> REGULAR_PRICE;
        };
    }

    private BookingResponse toResponse(MovieBookingDetails movieBookingDetails) {
        MovieShows movieShows = movieBookingDetails.getMovieShows();
        List<BookingResponse.BookingItemResponse> seatResponses = movieBookingDetails.getItems().stream()
                .map(item -> BookingResponse.BookingItemResponse.builder()
                        .showSeatId(item.getMovieShowSeat().getId())
                        .rowLabel(item.getMovieShowSeat().getRowLabel())
                        .seatNumber(item.getMovieShowSeat().getSeatNumber())
                        .seatType(item.getMovieShowSeat().getSeatType().name())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(movieBookingDetails.getId())
                .status(movieBookingDetails.getStatus().name())
                .showId(movieShows.getId())
                .movieTitle(movieShows.getMovieDetails().getTitle())
                .theatreName(movieShows.getTheatre().getName())
                .language(movieShows.getLanguage())
                .showDate(movieShows.getShowDate())
                .startTime(movieShows.getStartTime())
                .customerName(movieBookingDetails.getCustomerName())
                .customerEmail(movieBookingDetails.getCustomerEmail())
                .customerPhone(movieBookingDetails.getCustomerPhone())
                .totalSeats(movieBookingDetails.getTotalSeats())
                .totalAmount(movieBookingDetails.getTotalAmount())
                .bookedAt(movieBookingDetails.getBookedAt())
                .seats(seatResponses)
                .build();
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {

        log.info("Cancelling booking: {}", bookingId);

        MovieBookingDetails booking = bookingRepository.findByIdWithItemsForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.info("Booking already cancelled: {}", bookingId);
            return;
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED bookings can be cancelled");
        }

        MovieShows show = booking.getMovieShows();

        MovieShows lockedShow = showRepository.findByIdWithLock(show.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + show.getId()));

        if (lockedShow.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessException("Cannot cancel booking for this show");
        }

        List<MovieShowSeat> seats = booking.getItems().stream()
                .map(MovieBookingItem::getMovieShowSeat)
                .collect(Collectors.toList());

        for (MovieShowSeat seat : seats) {

            if (!seat.getMovieShows().getId().equals(lockedShow.getId())) {
                throw new BusinessException("Seat does not belong to the correct show: " + seat.getId());
            }

            if (seat.getStatus() != SeatStatus.BOOKED) {
                throw new BusinessException("Invalid seat state for cancellation: " + seat.getId());
            }

            seat.setStatus(SeatStatus.AVAILABLE);
        }

        showSeatRepository.saveAll(seats);

        lockedShow.setAvailableSeats(lockedShow.getAvailableSeats() + seats.size());
        showRepository.save(lockedShow);

        booking.setStatus(BookingStatus.CANCELLED);

        booking.setPaymentStatus(PaymentStatus.REFUNDED);

        bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", bookingId);
    }
}

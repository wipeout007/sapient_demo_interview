package com.project.movie.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class CreateBookingRequest {

    @NotNull(message = "Show ID is required")
    UUID showId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 255)
    String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    String customerPhone;

    @NotNull(message = "Seat IDs are required")
    @Size(min = 1, max = 10, message = "Between 1 and 10 seats can be booked at once")
    List<UUID> seatIds;
}

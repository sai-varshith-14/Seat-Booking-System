package com.platform.seatbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Show time is required")
    private LocalDateTime showTime;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;
}

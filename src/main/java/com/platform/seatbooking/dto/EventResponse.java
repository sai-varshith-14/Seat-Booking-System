package com.platform.seatbooking.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String venue;
    private LocalDateTime showTime;
    private int totalSeats;
}

package com.platform.seatbooking.dto;

import com.platform.seatbooking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long bookingId;
    private String eventTitle;
    private List<String> seatNumbers;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
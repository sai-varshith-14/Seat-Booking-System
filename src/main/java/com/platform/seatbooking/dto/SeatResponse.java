package com.platform.seatbooking.dto;

import com.platform.seatbooking.enums.SeatStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatResponse {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
}

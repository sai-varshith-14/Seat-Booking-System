package com.platform.seatbooking.controller;

import com.platform.seatbooking.dto.BookingRequest;
import com.platform.seatbooking.dto.BookingResponse;
import com.platform.seatbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingResponse> bookSeats(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid BookingRequest request) {
        BookingResponse response = bookingService.book(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader("X-User-Id") Long userId) {
        List<BookingResponse> bookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        bookingService.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }
}
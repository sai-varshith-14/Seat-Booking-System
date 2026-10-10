package com.platform.seatbooking.controller;

import com.platform.seatbooking.dto.CreateEventRequest;
import com.platform.seatbooking.dto.EventResponse;
import com.platform.seatbooking.dto.SeatResponse;
import com.platform.seatbooking.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody @Valid CreateEventRequest request) {
        EventResponse createdEvent = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(Pageable pageable) {
        Page<EventResponse> events = eventService.getEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long eventId) {
        List<SeatResponse> seats = eventService.getSeats(eventId);
        return ResponseEntity.ok(seats);
    }
}
package com.platform.seatbooking.service;

import com.platform.seatbooking.dto.CreateEventRequest;
import com.platform.seatbooking.dto.EventResponse;
import com.platform.seatbooking.dto.SeatResponse;
import com.platform.seatbooking.entity.Event;
import com.platform.seatbooking.entity.Seat;
import com.platform.seatbooking.enums.SeatStatus;
import com.platform.seatbooking.exception.ResourceNotFoundException;
import com.platform.seatbooking.repository.EventRepository;
import com.platform.seatbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    // Fix: changed private_final to private final
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .venue(request.getVenue())
                .showTime(request.getShowTime())
                .totalSeats(request.getTotalSeats())
                .build();

        Event savedEvent = eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= request.getTotalSeats(); i++) {
            Seat seat = Seat.builder()
                    .seatNumber("S" + i)
                    .status(SeatStatus.AVAILABLE)
                    .event(savedEvent)
                    .build();
            seats.add(seat);
        }

        seatRepository.saveAll(seats);

        return mapToEventResponse(savedEvent);
    }

    public Page<EventResponse> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(this::mapToEventResponse);
    }

    public List<SeatResponse> getSeats(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        List<Seat> seats = seatRepository.findByEventId(event.getId());
        return seats.stream()
                .map(seat -> SeatResponse.builder()
                        .id(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .status(seat.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    private EventResponse mapToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .venue(event.getVenue())
                .showTime(event.getShowTime())
                .totalSeats(event.getTotalSeats())
                .build();
    }
}
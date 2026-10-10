package com.platform.seatbooking.service;

import com.platform.seatbooking.dto.BookingRequest;
import com.platform.seatbooking.dto.BookingResponse;
import com.platform.seatbooking.entity.Booking;
import com.platform.seatbooking.entity.Event;
import com.platform.seatbooking.entity.Seat;
import com.platform.seatbooking.entity.User;
import com.platform.seatbooking.enums.BookingStatus;
import com.platform.seatbooking.enums.SeatStatus;
import com.platform.seatbooking.exception.ResourceNotFoundException;
import com.platform.seatbooking.exception.SeatUnavailableException;
import com.platform.seatbooking.exception.UnauthorizedActionException;
import com.platform.seatbooking.repository.BookingRepository;
import com.platform.seatbooking.repository.EventRepository;
import com.platform.seatbooking.repository.SeatRepository;
import com.platform.seatbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public BookingResponse book(Long userId, BookingRequest request) {
        // 1. Look up event and user
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + request.getEventId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // 2. Lock and fetch requested seats (Pessimistic Write Lock)
        List<Seat> seats = seatRepository.findAllByIdForUpdate(request.getSeatIds());

        // 3. Verify all requested seats exist and belong to the correct event
        if (seats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more requested seats were not found.");
        }
        for (Seat seat : seats) {
            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new UnauthorizedActionException("Seat " + seat.getId() + " does not belong to event " + event.getId());
            }
        }

        // 4. Verify each seat's status is AVAILABLE
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Seat " + seat.getSeatNumber() + " is already booked.");
            }
        }

        // 5. Create booking entity
        Booking booking = Booking.builder()
                .user(user)
                .event(event)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingTime(LocalDateTime.now())
                .build();

        // Mark seats as BOOKED and link them to the booking
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBooking(booking);
        }
        booking.setSeats(seats);

        // 6. Save the booking
        Booking savedBooking = bookingRepository.save(booking);

        // 7. Map and return response
        return mapToBookingResponse(savedBooking);
    }

    @Transactional
    public void cancel(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("Unauthorized: You do not own this booking");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new SeatUnavailableException("Booking is already cancelled");
        }

        // Mark booking as cancelled
        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Return seats to AVAILABLE state
        if (booking.getSeats() != null) {
            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBooking(null);
            }
        }

        bookingRepository.save(booking);
    }

    public List<BookingResponse> getMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<String> seatNumbers = booking.getSeats() != null ?
                booking.getSeats().stream().map(Seat::getSeatNumber).collect(Collectors.toList()) :
                List.of();

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .eventTitle(booking.getEvent().getTitle())
                .seatNumbers(seatNumbers)
                .status(booking.getBookingStatus())
                .createdAt(booking.getBookingTime())
                .build();
    }
}
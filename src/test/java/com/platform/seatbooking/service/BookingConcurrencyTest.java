package com.platform.seatbooking.service;

import com.platform.seatbooking.dto.BookingRequest;
import com.platform.seatbooking.entity.Event;
import com.platform.seatbooking.entity.Seat;
import com.platform.seatbooking.entity.User;
import com.platform.seatbooking.enums.Role;
import com.platform.seatbooking.enums.SeatStatus;
import com.platform.seatbooking.exception.SeatUnavailableException;
import com.platform.seatbooking.repository.BookingRepository;
import com.platform.seatbooking.repository.EventRepository;
import com.platform.seatbooking.repository.SeatRepository;
import com.platform.seatbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long testUserId;
    private Long testEventId;
    private Long testSeatId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create a test user
        User user = User.builder()
                .username("concurrency_user")
                .email("test@platform.com")
                .password("password")
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        testUserId = savedUser.getId();

        // 2. Create a test event
        Event event = Event.builder()
                .title("Concurrent Concert")
                .venue("Stadium Arena")
                .showTime(LocalDateTime.now().plusDays(5))
                .totalSeats(1)
                .build();
        Event savedEvent = eventRepository.save(event);
        testEventId = savedEvent.getId();

        // 3. Create a single test seat
        Seat seat = Seat.builder()
                .seatNumber("S1")
                .status(SeatStatus.AVAILABLE)
                .event(savedEvent)
                .build();
        Seat savedSeat = seatRepository.save(seat);
        testSeatId = savedSeat.getId();
    }

    @Test
    void testConcurrentBookingsForSameSeat() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BookingRequest request = new BookingRequest();
        request.setEventId(testEventId);
        request.setSeatIds(List.of(testSeatId));

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    bookingService.book(testUserId, request);
                    successCount.incrementAndGet();
                } catch (SeatUnavailableException e) {
                    // This is the expected rejection for concurrent threads losing the lock
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // Catch unexpected system errors (e.g., DB connection issues, null pointers)
                    System.err.println(">>> UNEXPECTED THREAD ERROR: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Trigger all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to finish
        finishLatch.await();
        executorService.shutdown();

        // Print explicit results to console
        System.out.println("==========================================");
        System.out.println(" CONCURRENCY TEST RESULTS");
        System.out.println(" Successful Bookings : " + successCount.get());
        System.out.println(" Failed/Blocked Bookings : " + failureCount.get());
        System.out.println("==========================================");

        // Assertions
        assertEquals(1, successCount.get(), "Only one booking should succeed!");
        assertEquals(threadCount - 1, failureCount.get(), "All other concurrent bookings should fail!");
    }
}
package com.platform.seatbooking.repository;

import com.platform.seatbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Custom query methods for events can be added here
}

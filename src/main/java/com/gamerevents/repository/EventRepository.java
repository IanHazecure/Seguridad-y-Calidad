package com.gamerevents.repository;

import com.gamerevents.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByOrderByStartDateAsc();

    List<Event> findByFeaturedTrueOrderByStartDateAsc();
}

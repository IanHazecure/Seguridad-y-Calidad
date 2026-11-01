package com.gamerevents.controller;

import com.gamerevents.dto.EventResponse;
import com.gamerevents.model.Event;
import com.gamerevents.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventRepository eventRepository;

    public EventApiController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<EventResponse> getEvents() {
        return eventRepository.findAllDetailedByOrderByStartDateAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return eventRepository.findDetailedById(id)
                .map(event -> ResponseEntity.ok(toResponse(event)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                event.getAddress(),
                event.getCategory(),
                event.isFeatured(),
                event.isParkingAvailable(),
                event.isPublicTransportAvailable(),
                event.isFoodServiceAvailable(),
                event.isAccommodationAvailable(),
                event.getOrganizer(),
                event.getMainSpeakers(),
                event.getGames().stream().map(game -> game.getName()).toList()
        );
    }
}

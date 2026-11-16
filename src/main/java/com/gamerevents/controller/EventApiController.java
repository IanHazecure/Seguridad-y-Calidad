package com.gamerevents.controller;

import com.gamerevents.dto.EventRequest;
import com.gamerevents.dto.EventResponse;
import com.gamerevents.model.Event;
import com.gamerevents.model.Game;
import com.gamerevents.model.User;
import com.gamerevents.repository.EventRepository;
import com.gamerevents.repository.GameRepository;
import com.gamerevents.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventRepository eventRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public EventApiController(EventRepository eventRepository,
                              GameRepository gameRepository,
                              UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
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

            @PostMapping
            public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request,
                                     @AuthenticationPrincipal UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
            Game game = gameRepository.findByName(request.gameName())
                .orElseGet(() -> gameRepository.save(new Game(request.gameName(), request.platform())));
            Event event = new Event(request.title(), request.description(), request.startDate(), request.endDate(),
                request.location(), request.address(), request.category());
            event.setOrganizer(request.organizer());
            event.setCreatedBy(user);
            event.getGames().add(game);
            Event savedEvent = eventRepository.save(event);
            return ResponseEntity.ok(toResponse(savedEvent));
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

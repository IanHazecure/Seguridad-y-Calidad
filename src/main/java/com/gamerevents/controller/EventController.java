package com.gamerevents.controller;

import com.gamerevents.model.Event;
import com.gamerevents.model.Game;
import com.gamerevents.model.User;
import com.gamerevents.repository.EventRepository;
import com.gamerevents.repository.GameRepository;
import com.gamerevents.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class EventController {

    private final EventRepository eventRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public EventController(EventRepository eventRepository,
                           GameRepository gameRepository,
                           UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/events")
    public String events(@RequestParam(required = false) String category,
                         @RequestParam(required = false) String location,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        LocalDateTime startDate = from == null ? null : from.atStartOfDay();
        LocalDateTime endDate = to == null ? null : to.plusDays(1).atStartOfDay().minusNanos(1);

        model.addAttribute("events", eventRepository.search(category, location, startDate, endDate));
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("location", location == null ? "" : location);
        model.addAttribute("from", from == null ? "" : from);
        model.addAttribute("to", to == null ? "" : to);
        return "events";
    }

    @GetMapping("/events/create")
    public String createEvent(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("formTitle", "Create event");
        model.addAttribute("formAction", "/events/create");
        return "event-form";
    }

    @PostMapping("/events/create")
    public String createEvent(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                              @RequestParam String location,
                              @RequestParam String address,
                              @RequestParam String category,
                              @RequestParam String gameName,
                              @RequestParam String platform,
                              @RequestParam String organizer,
                              @RequestParam(defaultValue = "false") boolean featured,
                              @RequestParam(defaultValue = "false") boolean parkingAvailable,
                              @RequestParam(defaultValue = "false") boolean publicTransportAvailable,
                              RedirectAttributes redirectAttributes) {
        if (!endDate.isAfter(startDate)) {
            redirectAttributes.addFlashAttribute("error", "End date must be after start date.");
            return "redirect:/events/create";
        }

        User user = findUser(userDetails);
        Event event = buildEvent(title, description, startDate, endDate, location, address, category,
                gameName, platform, organizer, featured, parkingAvailable, publicTransportAvailable, user);
        eventRepository.save(event);
        return "redirect:/events/" + event.getId();
    }

    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        return eventRepository.findById(id)
                .map(event -> {
                    if (!canManage(event, userDetails)) {
                        redirectAttributes.addFlashAttribute("error", "You cannot edit this event.");
                        return "redirect:/events";
                    }
                    model.addAttribute("event", event);
                    model.addAttribute("formTitle", "Edit event");
                    model.addAttribute("formAction", "/events/" + id + "/edit");
                    return "event-form";
                })
                .orElseGet(() -> "redirect:/events");
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                              @RequestParam String location,
                              @RequestParam String address,
                              @RequestParam String category,
                              @RequestParam String gameName,
                              @RequestParam String platform,
                              @RequestParam String organizer,
                              @RequestParam(defaultValue = "false") boolean featured,
                              @RequestParam(defaultValue = "false") boolean parkingAvailable,
                              @RequestParam(defaultValue = "false") boolean publicTransportAvailable,
                              RedirectAttributes redirectAttributes) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || !canManage(event, userDetails)) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit this event.");
            return "redirect:/events";
        }
        if (!endDate.isAfter(startDate)) {
            redirectAttributes.addFlashAttribute("error", "End date must be after start date.");
            return "redirect:/events/" + id + "/edit";
        }

        Game game = findOrCreateGame(gameName, platform);
        event.setTitle(title);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setAddress(address);
        event.setCategory(category);
        event.setOrganizer(organizer);
        event.setFeatured(featured);
        event.setParkingAvailable(parkingAvailable);
        event.setPublicTransportAvailable(publicTransportAvailable);
        event.getGames().clear();
        event.getGames().add(game);
        eventRepository.save(event);
        return "redirect:/events/" + id;
    }

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return eventRepository.findDetailedById(id)
                .map(event -> {
                    model.addAttribute("event", event);
                    return "event-details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Event not found.");
                    return "redirect:/events";
                });
    }

    private Event buildEvent(String title,
                             String description,
                             LocalDateTime startDate,
                             LocalDateTime endDate,
                             String location,
                             String address,
                             String category,
                             String gameName,
                             String platform,
                             String organizer,
                             boolean featured,
                             boolean parkingAvailable,
                             boolean publicTransportAvailable,
                             User user) {
        Event event = new Event(title, description, startDate, endDate, location, address, category);
        event.setOrganizer(organizer);
        event.setFeatured(featured);
        event.setParkingAvailable(parkingAvailable);
        event.setPublicTransportAvailable(publicTransportAvailable);
        event.setCreatedBy(user);
        event.getGames().add(findOrCreateGame(gameName, platform));
        return event;
    }

    private Game findOrCreateGame(String name, String platform) {
        return gameRepository.findByName(name)
                .orElseGet(() -> gameRepository.save(new Game(name, platform)));
    }

    private User findUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }

    private boolean canManage(Event event, UserDetails userDetails) {
        boolean administrator = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return administrator || (event.getCreatedBy() != null
                && event.getCreatedBy().getEmail().equals(userDetails.getUsername()));
    }
}

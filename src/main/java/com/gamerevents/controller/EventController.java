package com.gamerevents.controller;

import com.gamerevents.model.Event;
import com.gamerevents.repository.EventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
}

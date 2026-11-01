package com.gamerevents.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String address,
        String category,
        boolean featured,
        boolean parkingAvailable,
        boolean publicTransportAvailable,
        boolean foodServiceAvailable,
        boolean accommodationAvailable,
        String organizer,
        String mainSpeakers,
        List<String> games
) {
}

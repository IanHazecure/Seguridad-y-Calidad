package com.gamerevents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        @NotBlank String location,
        @NotBlank String address,
        @NotBlank String category,
        @NotBlank String gameName,
        @NotBlank String platform,
        @NotBlank String organizer
) {
}

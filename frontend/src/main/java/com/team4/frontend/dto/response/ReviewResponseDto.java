package com.team4.frontend.dto.response;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

public record ReviewResponseDto(
        Integer id,
        @NotNull CustomerResponseDto customer,
        @NotNull TripResponseDto trip,
        @NotNull Integer rating,
        String comment,
        Instant reviewDate
) implements Serializable {
}

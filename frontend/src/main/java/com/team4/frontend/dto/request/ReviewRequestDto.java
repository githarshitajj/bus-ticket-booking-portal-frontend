package com.team4.frontend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

public record ReviewRequestDto(
        Integer id,
        @NotNull Integer customerId,
        @NotNull Integer tripId,
        @NotNull Integer rating,
        String comment,
        Instant reviewDate
) implements Serializable {}

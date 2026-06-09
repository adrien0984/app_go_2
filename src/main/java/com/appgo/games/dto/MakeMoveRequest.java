package com.appgo.games.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MakeMoveRequest(
        @NotNull(message = "Row is required")
        @Min(value = 0, message = "Row must be >= 0")
        Integer row,

        @NotNull(message = "Column is required")
        @Min(value = 0, message = "Column must be >= 0")
        Integer col
) {
}

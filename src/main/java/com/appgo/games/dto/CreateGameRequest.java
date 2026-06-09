package com.appgo.games.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateGameRequest(
        @Min(value = 5, message = "Board size must be greater than or equal to 5")
        @Max(value = 25, message = "Board size must be lower than or equal to 25")
        Integer boardSize) {
}

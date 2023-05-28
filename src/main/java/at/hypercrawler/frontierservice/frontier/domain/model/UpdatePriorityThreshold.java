package at.hypercrawler.frontierservice.frontier.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePriorityThreshold(

        @Min(1)
        @NotNull
        Long days,

        @NotNull
        BigDecimal multiplier

) {
}

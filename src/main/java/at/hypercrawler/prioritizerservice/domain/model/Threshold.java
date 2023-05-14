package at.hypercrawler.prioritizerservice.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record Threshold(@Min(1) long days, @NotNull BigDecimal multiplier) {
}

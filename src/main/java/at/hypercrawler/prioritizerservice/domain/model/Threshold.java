package at.hypercrawler.prioritizerservice.domain.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Threshold {
    @Min(1) private long days;
    @NotNull private BigDecimal multiplier;
}

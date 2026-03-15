package com.jcc.helper.jcchelper.service.advice;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StructuredAdvice(
        @NotBlank String summary,
        @NotBlank String compDirection,
        @NotEmpty List<@NotBlank String> actions,
        @NotEmpty List<@NotBlank String> reasons,
        @NotEmpty List<@NotBlank String> risks,
        @NotNull List<@NotBlank String> uncertainties,
        @DecimalMin("0.0") @DecimalMax("1.0") double confidence
) {
}

package com.jcc.helper.jcchelper.api.dto;

public record QualityMetricsResponse(
        long totalAnalyzeCount,
        double fieldRecognitionSuccessRate,
        double lowConfidenceRatio,
        double suggestionStability,
        double latencyP50Ms,
        double latencyP95Ms
) {
}

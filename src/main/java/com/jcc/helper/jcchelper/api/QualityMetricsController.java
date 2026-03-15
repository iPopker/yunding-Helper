package com.jcc.helper.jcchelper.api;

import com.jcc.helper.jcchelper.api.dto.QualityMetricsResponse;
import com.jcc.helper.jcchelper.service.metrics.QualityMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class QualityMetricsController {

    private final QualityMetricsService qualityMetricsService;

    public QualityMetricsController(QualityMetricsService qualityMetricsService) {
        this.qualityMetricsService = qualityMetricsService;
    }

    @GetMapping("/quality")
    public QualityMetricsResponse quality() {
        return qualityMetricsService.snapshot();
    }
}

package com.jcc.helper.jcchelper.api;

import com.jcc.helper.jcchelper.api.dto.AnalyzeResponse;
import com.jcc.helper.jcchelper.service.AnalyzeService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1")
public class AnalyzeController {

    private final AnalyzeService analyzeService;

    public AnalyzeController(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @PostMapping(path = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyze(
            @RequestParam("gameId") @NotBlank String gameId,
            @RequestParam("image") MultipartFile image
    ) {
        return analyzeService.analyze(gameId, image);
    }
}
